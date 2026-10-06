package com.beacepl.service_request_service.service.impl;

import com.beacepl.service_request_service.ServiceRequestNotificationUtil;
import com.beacepl.service_request_service.client.OnboardingServiceClient;
import com.beacepl.service_request_service.MinioUtil;
import com.beacepl.service_request_service.client.OtpClient;
import com.beacepl.service_request_service.entity.FieldConfigEntity;
import com.beacepl.service_request_service.entity.OtpVerification;
import com.beacepl.service_request_service.entity.ServiceRequestAuditEntity;
import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.entity.ServiceRequestEntity;
import com.beacepl.service_request_service.enums.ServiceRequestStatus;
import com.beacepl.service_request_service.exceptions.ConfigurationNotFoundException;
import com.beacepl.service_request_service.exceptions.DuplicateRequestException;
import com.beacepl.service_request_service.exceptions.InvalidRequestException;
import com.beacepl.service_request_service.exceptions.OtpNotFoundException;
import com.beacepl.service_request_service.exceptions.OtpNotMatchException;
import com.beacepl.service_request_service.exceptions.RequestNotFoundException;
import com.beacepl.service_request_service.model.AccountSnapshot;
import com.beacepl.service_request_service.model.FieldChangeDetail;
import com.beacepl.service_request_service.model.ServiceRequestSubmitDto;
import com.beacepl.service_request_service.model.ServiceResponse;
import com.beacepl.service_request_service.repository.OtpRepository;
import com.beacepl.service_request_service.repository.ServiceRequestAuditRepository;
import com.beacepl.service_request_service.repository.ServiceRequestConfigRepository;
import com.beacepl.service_request_service.repository.ServiceRequestRepository;
import com.beacepl.service_request_service.service.AccountSnapshotService;
import com.beacepl.service_request_service.service.handler.ServiceRequestHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.beacepl.service_request_service.model.ServiceConfigAndOldValuesResponseDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceRequestServiceImpl {

    private final ServiceRequestConfigRepository configRepository;
    private final ServiceRequestRepository requestRepository;
    private final ServiceRequestAuditRepository auditRepository;
    private final FieldValidationService validationService;
    private final MinioService minioService;
    private final List<ServiceRequestHandler> requestHandlers;
    private final OtpRepository otpRepository;
    private final OtpClient otpClient;
    private final AccountSnapshotService accountSnapshotService;
    private final OldValueResolver oldValueResolver;
    private final ServiceRequestNotificationUtil serviceRequestNotificationUtil;

    public ServiceResponse<ServiceRequestEntity> submitServiceRequest(
            ServiceRequestSubmitDto submitDto,
            Map<String, MultipartFile> uploadedFiles) throws Exception {
        if (submitDto == null || submitDto.getServiceName() == null || submitDto.getServiceName().isBlank()) {
            throw new InvalidRequestException("serviceName is required");
        }

        String serviceName = submitDto.getServiceName();
        log.info("Processing service request submission for service: {}", serviceName);

        // 1. Retrieve Service Configuration
        ServiceRequestConfigEntity config = configRepository.findByServiceNameAndActiveTrue(serviceName)
                .orElseThrow(() -> new ConfigurationNotFoundException(
                        "Active service configuration not found for service: " + serviceName));

        // 2. Account Identification & Validation
        String accountIdentifier = getAccountIdentifier(submitDto);
        AccountSnapshot account = accountSnapshotService.getAccountById(accountIdentifier);

        String accountId = (account.getId() != null) ? account.getId() : submitDto.getAccountId();
        String investorCode = (account.getInvestorCode() != null) ? account.getInvestorCode()
                : submitDto.getInvestorCode();
        String mobileNumber = (account.getMobileNumber() != null) ? account.getMobileNumber()
                : submitDto.getMobileNumber();
        String email = (account.getEmailAddress() != null) ? account.getEmailAddress() : submitDto.getEmail();
        String platformId = (account.getPlatformId() != null) ? account.getPlatformId() : submitDto.getPlatformId();

        // 3. OTP Verification Flow
        if (submitDto.getOtp() == null || submitDto.getOtp().trim().isEmpty()) {
            String otpValue = String.format("%06d", new java.security.SecureRandom().nextInt(999999));
            log.info("Generated OTP value: {} for mobile: {}, email: {}", otpValue, mobileNumber, email);

            OtpVerification otpVerification = OtpVerification.builder()
                    .identifier(mobileNumber != null ? mobileNumber : email)
                    .otp(otpValue)
                    .processName(serviceName)
                    .createdAt(new java.util.Date())
                    .build();
            otpRepository.save(otpVerification);

            try {
                otpClient.sendOtp(mobileNumber, email, otpValue);
            } catch (Exception e) {
                log.warn("Failed to send OTP via OtpClient: {}", e.getMessage());
            }

            throw new OtpNotFoundException("OTP is required and has been sent to mobile/email");
        }

        boolean isOtpValid = false;
        List<OtpVerification> historicalOtps = otpRepository.findByIdentifierAndProcessNameOrderByCreatedAtDesc(
                mobileNumber != null ? mobileNumber : email, serviceName);
        if (!historicalOtps.isEmpty() && submitDto.getOtp().equals(historicalOtps.get(0).getOtp())) {
            isOtpValid = true;
        } else {
            try {
                isOtpValid = otpClient.validateOtp(mobileNumber, email, submitDto.getOtp());
            } catch (Exception e) {
                log.warn("OtpClient validation call error: {}", e.getMessage());
            }
        }

        if (!isOtpValid) {
            log.warn("OTP validation failed for identifier: {}", mobileNumber);
            throw new OtpNotMatchException("OTP validation failed");
        }

        // 4. Duplicate Pending Request Protection (Requirement 37)
        if (!"NOMINEE_ADD".equalsIgnoreCase(serviceName)) {
            boolean duplicateExists = requestRepository.existsByAccountIdAndServiceNameAndStatus(
                    accountId, serviceName, ServiceRequestStatus.PENDING);
            if (duplicateExists) {
                throw new DuplicateRequestException("A pending request for service '" + config.getDisplayName()
                        + "' already exists for this account.");
            }
        }

        // 5. Validate Action
        String action = (submitDto.getAction() != null && !submitDto.getAction().isBlank())
                ? submitDto.getAction().toUpperCase()
                : (config.getAllowedActions() != null && !config.getAllowedActions().isEmpty()
                        ? config.getAllowedActions().get(0)
                        : "EDIT");

        // 6. Dynamic Field Validation
        Map<String, Object> submittedFields = submitDto.getFieldValues() != null ? submitDto.getFieldValues()
                : new HashMap<>();
        validationService.validateServiceAndFields(
                config, action, submitDto.getListItemIdentifierValue(), submittedFields, uploadedFiles);

        // 7. Handle File Uploads to MinIO
        String generateId = "SR-" + UUID.randomUUID().toString().substring(0, 8);
        Map<String, String> uploadedFileNames = new HashMap<>();

        if (config.getFields() != null && uploadedFiles != null && !uploadedFiles.isEmpty()) {
            for (FieldConfigEntity fieldConfig : config.getFields()) {
                if (fieldConfig.isFile() || fieldConfig.getDataType().name().equals("FILE")) {
                    MultipartFile file = uploadedFiles.get(fieldConfig.getFieldName());
                    if (file != null && !file.isEmpty()) {
                        String objectName = minioService.upload(
                                file, investorCode, generateId, fieldConfig.getFieldName());
                        uploadedFileNames.put(fieldConfig.getFieldName(), objectName);
                    }
                }
            }
        }

        // 8. Resolve Strategy Handler for Old / New Values
        ServiceRequestHandler handler = requestHandlers.stream()
                .filter(h -> h.supports(config))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException("No request handler found for service config: " + serviceName));

        Map<String, Object> oldValues = handler.resolveOldValues(
                account, config, submitDto.getListItemIdentifierValue(), submittedFields);
        Map<String, Object> newValues = handler.resolveNewValues(
                submittedFields, uploadedFileNames, config);
        List<FieldChangeDetail> fieldDetails = handler.buildFieldDetails(
                oldValues, newValues, config);

        // 9. Build & Save Service Request Entity
        ServiceRequestEntity entity = ServiceRequestEntity.builder()
                .id(generateId)
                .accountId(accountId)
                .investorCode(investorCode)
                .mobileNumber(mobileNumber)
                .email(email)
                .serviceName(serviceName)
                .displayName(config.getDisplayName())
                .section(config.getSection())
                .action(action)
                .oldValues(oldValues)
                .newValues(newValues)
                .fieldDetails(fieldDetails)
                .platformid(platformId)
                .listItemIdentifierValue(submitDto.getListItemIdentifierValue())
                .status(ServiceRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ServiceRequestEntity savedEntity = requestRepository.save(entity);
        log.info("Service request created successfully with ID: {} for account: {}", savedEntity.getId(), accountId);

        // 10. Save Audit Information
        auditRepository.save(ServiceRequestAuditEntity.builder()
                .requestId(savedEntity.getId())
                .serviceName(serviceName)
                .accountId(accountId)
                .investorCode(investorCode)
                .action(action)
                .status(ServiceRequestStatus.PENDING.name())
                .performedBy(email != null ? email : mobileNumber)
                .remark("Service request submitted")
                .timestamp(LocalDateTime.now())
                .build());

        serviceRequestNotificationUtil.sendNotification(savedEntity, savedEntity.getStatus());

        return ServiceResponse.success("Service request submitted successfully.", enrichWithPresignedUrls(savedEntity));
    }

    public ServiceResponse<Page<ServiceRequestEntity>> getUserRequests(
            String identifier,
            ServiceRequestStatus status,
            String serviceName,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ServiceRequestEntity> requests;

        if (status != null) {
            requests = requestRepository.findByAccountIdAndStatus(identifier, status, pageable);
        } else {
            requests = requestRepository.findByAccountId(identifier, pageable);
            if (requests.isEmpty()) {
                requests = requestRepository.findByMobileNumberOrEmailOrInvestorCodeOrAccountId(
                        identifier, identifier, identifier, identifier, pageable);
            }
        }

        Page<ServiceRequestEntity> presignedRequests = requests.map(this::enrichWithPresignedUrls);

        return ServiceResponse.success("User request history fetched successfully", presignedRequests);
    }

    public ServiceResponse<ServiceRequestEntity> getRequestById(String id) {
        ServiceRequestEntity entity = requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Service request not found with ID: " + id));
        return ServiceResponse.success(enrichWithPresignedUrls(entity));
    }

    private String getAccountIdentifier(ServiceRequestSubmitDto dto) {
        if (dto.getAccountId() != null && !dto.getAccountId().isBlank())
            return dto.getAccountId();
        if (dto.getInvestorCode() != null && !dto.getInvestorCode().isBlank())
            return dto.getInvestorCode();
        if (dto.getMobileNumber() != null && !dto.getMobileNumber().isBlank())
            return dto.getMobileNumber();
        if (dto.getEmailAddress() != null && !dto.getEmailAddress().isBlank())
            return dto.getEmailAddress();
        throw new InvalidRequestException(
                "Account identifier (accountId, investorCode, mobileNumber, or email) is required.");
    }

    public ServiceResponse<ServiceConfigAndOldValuesResponseDto> getServiceConfigWithOldValues(
            String investorCode,
            String serviceName,
            String listItemIdentifierValue
    ) {
        if (serviceName == null || serviceName.isBlank()) {
            throw new InvalidRequestException("serviceName is required");
        }
        if (investorCode == null || investorCode.isBlank()) {
            throw new InvalidRequestException("investorCode is required");
        }

        // 1. Fetch active service configuration
        ServiceRequestConfigEntity config = configRepository.findByServiceNameAndActiveTrue(serviceName)
                .orElseThrow(() -> new ConfigurationNotFoundException("Active service configuration not found for service: " + serviceName));

        // 2. Fetch current account snapshot from local AccountEntity in MongoDB
        AccountSnapshot account = accountSnapshotService.getAccountById(investorCode);

        // 3. Resolve current old values from AccountEntity
        Map<String, Object> oldValues = oldValueResolver.resolveOldValues(account, config, listItemIdentifierValue, null);

        // 4. Map configuration fields merged with old value & value type
        List<ServiceConfigAndOldValuesResponseDto.FieldConfigWithValueDto> fieldDtos = new ArrayList<>();
        if (config.getFields() != null) {
            for (FieldConfigEntity field : config.getFields()) {
                String path = (field.getAccountFieldPath() != null && !field.getAccountFieldPath().isBlank())
                        ? field.getAccountFieldPath()
                        : field.getFieldName();

                Object rawOldVal = null;
                if (oldValues != null) {
                    rawOldVal = oldValues.get(path);
                    if (rawOldVal == null) {
                        rawOldVal = oldValues.get(field.getFieldName());
                    }
                    if (rawOldVal == null && path.contains(".")) {
                        String leaf = path.substring(path.lastIndexOf('.') + 1);
                        rawOldVal = oldValues.get(leaf);
                    }
                }

                // If file or image, resolve MinIO presigned URL using MinioUtil
                if (rawOldVal instanceof String s && !s.isBlank()) {
                    if (field.isFile()
                            || (field.getDataType() != null && "FILE".equalsIgnoreCase(field.getDataType().name()))
                            || isLikelyFileKeyOrName(field.getFieldName(), s)) {
                        rawOldVal = MinioUtil.getImageUrl(s);
                    }
                }

                String valueType = (field.getDataType() != null) ? field.getDataType().name() : "STRING";
                if (rawOldVal != null) {
                    valueType = rawOldVal.getClass().getSimpleName();
                }

                fieldDtos.add(ServiceConfigAndOldValuesResponseDto.FieldConfigWithValueDto.builder()
                        .fieldName(field.getFieldName())
                        .label(field.getLabel())
                        .dataType(field.getDataType())
                        .valueType(valueType)
                        .required(field.isRequired())
                        .validationRegex(field.getValidationRegex())
                        .minLength(field.getMinLength())
                        .maxLength(field.getMaxLength())
                        .accountFieldPath(field.getAccountFieldPath())
                        .isFile(field.isFile())
                        .oldValue(rawOldVal)
                        .build());
            }
        }

        ServiceConfigAndOldValuesResponseDto dto = ServiceConfigAndOldValuesResponseDto.builder()
                .serviceName(config.getServiceName())
                .displayName(config.getDisplayName())
                .description(config.getDescription())
                .section(config.getSection())
                .sectionDisplayName(config.getSectionDisplayName())
                .isListBased(config.isListBased())
                .targetListField(config.getTargetListField())
                .listIdentifierField(config.getListIdentifierField())
                .allowedActions(config.getAllowedActions())
                .fields(fieldDtos)
                .build();

        return ServiceResponse.success("Service configuration and old values fetched successfully", dto);
    }

    private boolean isLikelyFileKeyOrName(String fieldName, String value) {
        if (value == null || value.isBlank()) return false;
        String lowerVal = value.toLowerCase();
        if (lowerVal.endsWith(".jpg") || lowerVal.endsWith(".jpeg") || lowerVal.endsWith(".png")
                || lowerVal.endsWith(".pdf") || lowerVal.endsWith(".webp") || lowerVal.endsWith(".gif")) {
            return true;
        }
        if (fieldName != null) {
            String lowerField = fieldName.toLowerCase();
            return lowerField.contains("file") || lowerField.contains("photo") || lowerField.contains("signature")
                    || lowerField.contains("certificate") || lowerField.contains("cheque") || lowerField.contains("nidfront")
                    || lowerField.contains("nidback") || lowerField.contains("document") || lowerField.contains("attachment");
        }
        return false;
    }

    public ServiceRequestEntity enrichWithPresignedUrls(ServiceRequestEntity entity) {
        if (entity == null) {
            return null;
        }

        Map<String, Object> presignedOldValues = convertFileObjectNamesToPresignedUrls(entity.getOldValues());
        Map<String, Object> presignedNewValues = convertFileObjectNamesToPresignedUrls(entity.getNewValues());

        List<FieldChangeDetail> presignedDetails = null;
        if (entity.getFieldDetails() != null) {
            presignedDetails = entity.getFieldDetails().stream()
                    .map(detail -> {
                        Object oldVal = detail.getOldValue();
                        Object newVal = detail.getNewValue();
                        if (detail.isFile() || "FILE".equalsIgnoreCase(detail.getDataType())) {
                            if (oldVal instanceof String s && (isLikelyFileObjectName(s) || isLikelyFileKeyOrName(detail.getFieldName(), s))) {
                                oldVal = MinioUtil.getImageUrl(s);
                            }
                            if (newVal instanceof String s && (isLikelyFileObjectName(s) || isLikelyFileKeyOrName(detail.getFieldName(), s))) {
                                newVal = MinioUtil.getImageUrl(s);
                            }
                        }
                        return FieldChangeDetail.builder()
                                .fieldName(detail.getFieldName())
                                .label(detail.getLabel())
                                .dataType(detail.getDataType())
                                .isFile(detail.isFile())
                                .oldValue(oldVal)
                                .newValue(newVal)
                                .build();
                    })
                    .toList();
        }

        entity.setOldValues(presignedOldValues);
        entity.setNewValues(presignedNewValues);
        entity.setFieldDetails(presignedDetails);
        return entity;
    }

    private Map<String, Object> convertFileObjectNamesToPresignedUrls(Map<String, Object> values) {
        if (values == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof String strVal) {
                if (isLikelyFileObjectName(strVal) || isLikelyFileKeyOrName(entry.getKey(), strVal)) {
                    result.put(entry.getKey(), MinioUtil.getImageUrl(strVal));
                    continue;
                }
            }
            result.put(entry.getKey(), val);
        }
        return result;
    }

    private boolean isLikelyFileObjectName(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String lower = value.toLowerCase();

        return lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".pdf")
                || lower.endsWith(".webp")
                || lower.contains("tin_certificate")
                || lower.contains("-nominee-")
                || lower.contains("nid_front")
                || lower.contains("nid_back")
                || lower.contains("photo")
                || lower.contains("tin");
    }
}
