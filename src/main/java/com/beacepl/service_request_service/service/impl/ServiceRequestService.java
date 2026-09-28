package com.beacepl.service_request_service.service.impl;

import com.beacepl.service_request_service.MinioUtil;
import com.beacepl.service_request_service.client.AccountServiceClient;
import com.beacepl.service_request_service.client.OtpClient;
import com.beacepl.service_request_service.entity.ChangeRequestEntity;
import com.beacepl.service_request_service.entity.FieldConfigEntity;
import com.beacepl.service_request_service.entity.OtpVerification;
import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.enums.RequestAction;
import com.beacepl.service_request_service.enums.RequestStatus;
import com.beacepl.service_request_service.exceptions.OtpNotFoundException;
import com.beacepl.service_request_service.exceptions.OtpNotMatchException;
import com.beacepl.service_request_service.model.*;
import com.beacepl.service_request_service.repository.ChangeRequestRepository;
import com.beacepl.service_request_service.repository.OtpRepository;
import com.beacepl.service_request_service.repository.ServiceRequestConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private final ServiceRequestConfigRepository configRepository;
    private final ChangeRequestRepository changeRequestRepository;
    private final ServiceRequestValidator validator;
//    private final FileStorageService fileStorageService;
    private final MinioService minioService;
    private final AccountServiceClient accountServiceClient;
    private final OtpRepository otpRepository;
    private final OtpClient otpClient;
    private final NotificationClientService notificationClientService;

    /**
     * Single entry point for every service request type. "files" is empty
     * for pure-JSON submissions (e.g. MOBILE_CHANGE, EMAIL_CHANGE) and
     * populated for multipart submissions (e.g. BANK_CHANGE, TIN_CHANGE,
     * NOMINEE_ADD/EDIT). Which fields are actually files, required, etc.
     * all comes from the stored ServiceRequestConfigEntity - this method
     * itself has no knowledge of individual service types.
     */
    public ServiceResponse submitServiceRequest(ServiceRequestSubmitDto dto, Map<String, MultipartFile> files) throws Exception {
        boolean otpValid = false;
        if (dto.getServiceName() == null) {
            throw new IllegalArgumentException("serviceName is required");
        }

        if (dto.getOtp() == null || dto.getOtp().isEmpty()){
            String otpValue = String.format("%06d", new java.security.SecureRandom().nextInt(999999));
            log.info("OTP VALUE IS : {}", otpValue);

//                // Persist verification entry to DB
            OtpVerification otpVerification = OtpVerification.builder()
                    .identifier(dto.getMobileNumber())
                    .otp(otpValue)
                    .processName(dto.getServiceName())
                    .createdAt(new java.util.Date())
                    .build();
            otpRepository.save(otpVerification);
            otpClient.sendOtp(dto.getMobileNumber(), dto.getEmailAddress(), otpValue);
            throw new OtpNotFoundException("Otp is sent to " + dto.getEmailAddress()+ " and "+ dto.getMobileNumber());
        }
        else {
            List<OtpVerification> historicalOtps = otpRepository
                    .findByIdentifierAndProcessNameOrderByCreatedAtDesc(dto.getMobileNumber(), dto.getServiceName());

            if (historicalOtps.isEmpty()) {
                throw new OtpNotFoundException( "Otp is sent to " + dto.getEmailAddress()+ " and "+ dto.getMobileNumber());
            }

            OtpVerification latestOtpRecord = historicalOtps.get(0);
            if (dto.getOtp().equals(latestOtpRecord.getOtp())){
                otpValid = true;
            }
        }

        if (!otpValid) {
            log.warn("Invalid OTP for user: {}", dto.getEmailAddress() + dto.getMobileNumber());
            throw new OtpNotMatchException("Invalid OTP");
        }

        ServiceRequestConfigEntity config = configRepository
                .findByServiceNameAndActiveTrue(dto.getServiceName())
                .orElseThrow(() -> new IllegalArgumentException("Unknown or inactive service: " + dto.getServiceName()));

        RequestAction action = dto.getAction() == null ? RequestAction.EDIT : dto.getAction();
        if (config.getAllowedActions() != null && !config.getAllowedActions().isEmpty()
                && !config.getAllowedActions().contains(action.name())) {
            throw new IllegalArgumentException("Action " + action + " is not allowed for service " + dto.getServiceName());
        }

        if (config.isListBased() && (action == RequestAction.EDIT || action == RequestAction.REMOVE)
                && !org.springframework.util.StringUtils.hasText(dto.getListItemIdentifierValue())) {
            throw new IllegalArgumentException(
                    "listItemIdentifierValue is required for " + action + " on service " + dto.getServiceName());
        }

        Map<String, Object> fieldValues = dto.getFieldValues() == null
                ? new HashMap<>()
                : new HashMap<>(dto.getFieldValues());
        // REMOVE actions carry no field values (there is nothing to change, just delete the item)
        if (action != RequestAction.REMOVE) {
            validator.validate(config, fieldValues, files);

            // Resolve every configured FILE field: store the upload, replace the
            // raw MultipartFile with the persisted path/URL in fieldValues.
            for (FieldConfigEntity field : config.getFields()) {
                if (!field.isFile()) {
                    continue;
                }
                MultipartFile file = files == null ? null : files.get(field.getFieldName());
                if (file != null && !file.isEmpty()) {
//                    String storedPath = fileStorageService.store(file, config.getServiceName(), field.getFieldName());
                    String storedPath = minioService.upload(file, field.getFieldName(), dto.getMobileNumber()+dto.getAccountId());
                    fieldValues.put(field.getFieldName(), storedPath);
                }
            }
        }

        ChangeRequestEntity entity = ChangeRequestEntity.builder()
                .accountId(dto.getAccountId())
                .mobileNumber(dto.getMobileNumber())
                .serviceName(config.getServiceName())
                .action(action)
                .fieldValues(fieldValues)
                .listItemIdentifierValue(dto.getListItemIdentifierValue())
                .status(RequestStatus.PENDING)
                .oldValues(dto.getOldValues())
                .fieldTypes(dto.getFieldTypes())
                .investorCode(dto.getInvestorCode())
                .build();

        entity = changeRequestRepository.save(entity);
        this.sendNotificationForWithdrawalForManagement(entity);
        log.info("Created change request {} for service {} / account {}", entity.getId(), config.getServiceName(), dto.getAccountId());

        return new ServiceResponse(false, "Request submitted successfully and is pending approval", entity, "200");
    }

    public ServiceResponse getPreviousServiceRequests(String mobileNumber) {
        List<ChangeRequestEntity> requests = changeRequestRepository.findByMobileNumberOrderByCreatedAtDesc(mobileNumber);
        return new ServiceResponse(false, "Previous service requests fetched", requests, "200");
    }

    /** Returns every active service type - drives the "what can I request?" menu on the client. */
    public ServiceResponse getServiceRequestList() {
        List<ServiceRequestConfigEntity> configs = configRepository.findByActiveTrue();

        configs.sort(
                Comparator
                        .comparing((ServiceRequestConfigEntity c) ->
                                c.getSectionOrder() == null ? Integer.MAX_VALUE : c.getSectionOrder())
                        .thenComparing(c ->
                                c.getDisplayOrder() == null ? Integer.MAX_VALUE : c.getDisplayOrder())
        );

        Map<String, List<ServiceRequestConfigEntity>> grouped = configs.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getSection() == null ? "OTHER" : c.getSection(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<ServiceSectionDto> sections = grouped.entrySet().stream()
                .map(e -> {
                    String label = e.getValue().get(0).getSectionDisplayName() != null
                            ? e.getValue().get(0).getSectionDisplayName()
                            : e.getKey();
                    return new ServiceSectionDto(e.getKey(), label, e.getValue());
                })
                .collect(Collectors.toList());

        return new ServiceResponse(false, "Service request list fetched", sections, "200");
    }

    /**
     * Returns the field configuration for one service type, so the client
     * can render a form dynamically, plus the requesters prior requests
     * for that service (useful for showing current pending/rejected state).
     */
    public ServiceResponse getServiceRequestListByName(String name, String mobileNumber) {
//        ServiceRequestConfigEntity config = configRepository.findByServiceNameAndActiveTrue(name)
//                .orElseThrow(() -> new IllegalArgumentException("Unknown or inactive service: " + name));
//
//        List<ChangeRequestEntity> history = CollectionUtils.isEmpty(changeRequestRepository
//                .findByServiceNameAndMobileNumberOrderByCreatedAtDesc(name, mobileNumber))
//                ? List.of()
//                : changeRequestRepository.findByServiceNameAndMobileNumberOrderByCreatedAtDesc(name, mobileNumber);
//
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("config", config);
//        payload.put("history", history);

        ServiceRequestConfigEntity config = configRepository
                .findByServiceNameAndActiveTrue(name)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unknown or inactive service: " + name
                        )
                );

        AccountSnapshot snapshot =
                accountServiceClient.getAccountSnapshot(mobileNumber);

        List<Request> requests = new ArrayList<>();

        if (config.getFields() != null && snapshot != null) {

            // ============================
            // LIST BASED
            // ============================
            if (config.isListBased()) {

                Object listObject = getValueByPath(
                        snapshot,
                        config.getTargetListField()
                );

                if (listObject instanceof List<?> list && !list.isEmpty()) {

                    for (Object listItem : list) {          // ← loop every nominee

                        Map<String, Object> oldValues = new HashMap<>();

                        for (FieldConfigEntity field : config.getFields()) {

                            Object oldValue = getValueByPath(
                                    listItem,
                                    field.getAccountFieldPath()
                            );

                            oldValues.put(
                                    this.formatFieldName(field.getFieldName()),
                                    oldValue != null ? oldValue : ""
                            );
                        }

                        // one Request object per nominee
                        requests.add(
                                Request.builder()
                                        .oldValues(oldValues)
                                        .build()
                        );
                    }
                }

            }

            // ============================
            // NORMAL ACCOUNT FIELDS
            // ============================
            else {

                for (FieldConfigEntity field : config.getFields()) {

                    Object oldValue = getValueByPath(
                            snapshot,
                            field.getAccountFieldPath()
                    );

                    Map<String, Object> oldValues = new HashMap<>();

                    oldValues.put(
                            this.formatFieldName(field.getFieldName()),
                            oldValue != null ? oldValue : ""
                    );

                    requests.add(
                            Request.builder()
                                    .oldValues(oldValues)
                                    .build()
                    );
                }
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("config", config);
        payload.put("requests", requests);



        return new ServiceResponse(false, "Form fields fetched", payload, "200");
    }

    private Object getValueByPath(Object object, String path) {
        if (object == null || path == null || path.isBlank()) {
            return null;
        }

        Object current = object;

        try {
            for (String fieldName : path.split("\\.")) {

                if (current == null) {
                    return null;
                }

                PropertyDescriptor property =
                        new PropertyDescriptor(fieldName, current.getClass());

                Method getter = property.getReadMethod();

                if (getter == null) {
                    return null;
                }

                current = getter.invoke(current);
            }

            return current;

        } catch (Exception e) {
            return null;
        }
    }

    private void sendNotificationForWithdrawalForManagement(ChangeRequestEntity changeRequestEntity ){
        String message = String.format("Service request from your BESL A/C %s  has been received for %s .",
                changeRequestEntity.getMobileNumber(), changeRequestEntity.getServiceName());
        String messageForRm = String.format("Service request from your client %s has been received for %s .",
                changeRequestEntity.getMobileNumber(),changeRequestEntity.getServiceName());
        String messageForManagement = String.format("Service request from client %s has been received for %s .",
                changeRequestEntity.getMobileNumber(), changeRequestEntity.getServiceName());
        notificationClientService.sendUnicast(
                changeRequestEntity.getMobileNumber(),
                "Service Request Notification",
                message,
                "INFO",
                Map.of(
                        "serviceName", changeRequestEntity.getServiceName()
                ),
                "STATUS_PAGE");
        notificationClientService.sendUnicast(
                "01700000021",
                "Service Request Notification",
                messageForRm,
                "INFO",
                Map.of(
                        "serviceName", changeRequestEntity.getServiceName()
                ),
                "STATUS_PAGE");
        notificationClientService.sendUnicast(
                "01921008451",
                "Service Request Notification",
                messageForManagement,
                "INFO",
                Map.of(
                        "serviceName", changeRequestEntity.getServiceName()
                ),
                "STATUS_PAGE");
    }

    private String formatFieldName(String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return fieldName;
        }

        fieldName = fieldName.replaceFirst("(?i)^new[_-]?", "");

        fieldName = fieldName.replaceAll("(?<!^)([A-Z])", " $1");

        return fieldName.trim();
    }
}
