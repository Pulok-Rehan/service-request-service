//package com.beacepl.service_request_service.service.impl;
//
//import com.beacepl.service_request_service.ResponseMessageUtil;
//import com.beacepl.service_request_service.client.AccountServiceClient;
//import com.beacepl.service_request_service.client.OtpClient;
//import com.beacepl.service_request_service.entity.*;
//import com.beacepl.service_request_service.model.*;
//import com.beacepl.service_request_service.repository.ChangeRequestRepository;
//import com.beacepl.service_request_service.repository.OtpRepository;
//import com.beacepl.service_request_service.repository.ServiceCategoryRepository;
//import com.beacepl.service_request_service.service.ServiceRequestService;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.*;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class ServiceRequestServiceImpl implements ServiceRequestService {
//
//    private final ServiceCategoryRepository categoryRepository;
//    private final ChangeRequestRepository changeRequestRepository;
//    private final AccountServiceClient accountServiceClient;
//    private final OtpClient otpClient;
//    private final ObjectMapper objectMapper;
//    private final OtpRepository otpRepository;
//    private final ExternalServiceClient externalServiceClient;
//    private final NotificationClientService notificationClientService;
//    private final MinioService minioService;
//
//    @Override
//    public ServiceResponse getServiceRequestList() throws JsonProcessingException {
//        log.info("Fetching active service request categories and definitions");
//        List<ServiceCategory> servicesList = categoryRepository.findAllActiveServiceRequestsSorted();
//        if (servicesList.isEmpty()) {
//            return new ServiceResponse(false, "No service request found", Collections.emptyList(), "200");
//        }
//        return new ServiceResponse(false, "Service Request List found", objectMapper.writeValueAsString(servicesList), "200");
//    }
//
//    @Override
//    public ServiceResponse getServiceRequestListByName(String name, String mobileNumber) {
//        try {
//            log.info("Fetching form fields schema for service: {} and mobile: {}", name, mobileNumber);
//
//            Optional<ServiceCategory> serviceCategoryOpt = categoryRepository.findCategoryByServiceName(name);
//            ServiceRequestDefinition targetRequest = null;
//
//            if (serviceCategoryOpt.isPresent()) {
//                targetRequest = serviceCategoryOpt.get().getServiceRequestList().stream()
//                        .filter(sr -> sr.getName() != null && sr.getName().equalsIgnoreCase(name))
//                        .findFirst()
//                        .orElse(null);
//            } else {
//                targetRequest = categoryRepository.findServiceRequestByName(name).orElse(null);
//            }
//
//            if (targetRequest == null) {
//                return new ServiceResponse(true, "No service request found with name: " + name, null, "404");
//            }
//
//            // Fetch account snapshot
//            AccountSnapshot snapshot = accountServiceClient.getAccountSnapshot(mobileNumber);
//            Map<String, Object> snapshotMap = (snapshot != null)
//                    ? objectMapper.convertValue(snapshot, new TypeReference<Map<String, Object>>() {})
//                    : Collections.emptyMap();
//
//            Map<String, Object> bankMap = (snapshotMap.get("bank") != null)
//                    ? objectMapper.convertValue(snapshotMap.get("bank"), new TypeReference<Map<String, Object>>() {})
//                    : Collections.emptyMap();
//
//            List<Request> requests = new ArrayList<>();
//            if (targetRequest.getFormFields() != null) {
//                for (FormField field : targetRequest.getFormFields()) {
//                    String key = field.getParameterName();
//                    String label = field.getName();
//
//                    if (key == null || key.trim().isEmpty()) continue;
//
//                    Object value = null;
//                    if (snapshotMap.containsKey(key) && snapshotMap.get(key) != null) {
//                        value = snapshotMap.get(key);
//                    } else if (bankMap.containsKey(key) && bankMap.get(key) != null) {
//                        value = bankMap.get(key);
//                    }
//
//                    Map<String, Object> oldVal = new HashMap<>();
//                    oldVal.put(key, value != null ? value : "");
//
//                    Map<String, Object> requestedValue = new HashMap<>();
//                    requestedValue.put(key, "");
//
//                    String fieldAction = (field.getAction() != null) ? field.getAction()
//                            : (targetRequest.getAction() != null ? targetRequest.getAction() : "UPDATE");
//
//                    Request req = Request.builder()
//                            .label(label)
//                            .action(fieldAction)
//                            .oldValues(oldVal)
//                            .requestedValue(requestedValue)
//                            .build();
//
//                    requests.add(req);
//                }
//            }
//
//            Map<String, Object> result = new HashMap<>();
//            result.put("requests", requests);
//
//            return new ServiceResponse(false, "Service Request details found", objectMapper.writeValueAsString(result), "200");
//
//        } catch (JsonProcessingException e) {
//            log.error("Error serializing service request form details for name: {}", name, e);
//            return new ServiceResponse(true, "JSON Parsing Error: " + e.getMessage(), null, "500");
//        } catch (Exception e) {
//            log.error("Error fetching service request form schema for name: {}", name, e);
//            return new ServiceResponse(true, "Internal Server Error: " + e.getMessage(), null, "500");
//        }
//    }
//
//    @Override
//    public ServiceResponse submitServiceRequest(GenericServiceRequestDto requestDto) throws JsonProcessingException {
//        log.info("Processing dynamic service request for service: {}", requestDto.getServiceName());
//
//        if (requestDto.getOtp() == null || requestDto.getOtp().isEmpty()) {
//            String otpValue = String.format("%06d", new java.security.SecureRandom().nextInt(999999));
//            log.info("OTP VALUE IS : {}", otpValue);
//
//            // Persist verification entry to DB
//            OtpVerification otpVerification = OtpVerification.builder()
//                    .identifier(requestDto.getMobileNumber())
//                    .otp(otpValue)
//                    .processName("RESET_PASSWORD")
//                    .createdAt(new java.util.Date())
//                    .build();
//            otpRepository.save(otpVerification);
//            otpClient.sendOtp(requestDto.getMobileNumber(), requestDto.getEmail());
//            return ResponseMessageUtil.otpRequiredResponse(requestDto.getMobileNumber(), requestDto.getEmail());
//        }
//
//        // Validate OTP (allow test OTP "123456" or check via otpClient)
//        boolean isOtpValid = otpClient.validateOtp(requestDto.getMobileNumber(), requestDto.getEmail(), requestDto.getOtp());
//
//        if (!isOtpValid) {
//            return new ServiceResponse("OTP validation failed", "428");
//        }
//
//        // Fetch dynamic configuration metadata if present
//        Optional<ServiceRequestDefinition> configOpt = categoryRepository.findServiceRequestByName(requestDto.getServiceName());
//        if (configOpt.isPresent()) {
//            ServiceRequestDefinition config = configOpt.get();
//            if (config.getBody() != null && config.getBody().getRequiredFields() != null) {
//                validateRootFields(requestDto, config.getBody().getRequiredFields());
//            }
//            if (config.getFormFields() != null && !config.getFormFields().isEmpty()) {
//                validateRequestPayload(requestDto.getRequest(), config.getFormFields());
//            }
//        }
//
//        // Resolve action type (e.g., ADD / UPDATE)
//        String action = requestDto.getAction();
//        if ((action == null || action.isEmpty()) && configOpt.isPresent()) {
//            action = configOpt.get().getAction();
//        }
//        if (action == null || action.isEmpty()) {
//            action = "UPDATE";
//        }
//
//        // Normalize payload (convert fieldValues map to List<Request> if request list is empty)
//        List<Request> requestList = requestDto.getRequest();
//        if ((requestList == null || requestList.isEmpty()) && requestDto.getFieldValues() != null) {
//            requestList = new ArrayList<>();
//            for (Map.Entry<String, Object> entry : requestDto.getFieldValues().entrySet()) {
//                Map<String, Object> requestedVal = new HashMap<>();
//                requestedVal.put(entry.getKey(), entry.getValue());
//                requestList.add(Request.builder()
//                        .label(entry.getKey())
//                        .action(action)
//                        .requestedValue(requestedVal)
//                        .oldValues(Collections.emptyMap())
//                        .build());
//            }
//        } else if (requestList != null) {
//            for (Request req : requestList) {
//                if (req.getAction() == null || req.getAction().isEmpty()) {
//                    req.setAction(action);
//                }
//            }
//        }
//
//        ChangeRequestEntity entity = ChangeRequestEntity.builder()
//                .mobileNumber(requestDto.getMobileNumber())
//                .accountId(requestDto.getAccountId())
//                .email(requestDto.getEmail())
//                .requestedBy(requestDto.getEmail() != null ? requestDto.getEmail() : requestDto.getMobileNumber())
//                .requestedFor(requestDto.getServiceName())
//                .action(action)
//                .request(requestList)
//                .status("PENDING")
//                .requestedAt(LocalDateTime.now())
//                .userRemarks("Service request submitted for " + requestDto.getServiceName())
//                .userNotified(false)
//                .investorCode(requestDto.getInvestorCode())
//                .build();
//
//        ChangeRequestEntity saved = changeRequestRepository.save(entity);
//        notificationClientService.sendUnicast(entity.getMobileNumber(), "Service Request Notification", "A service request has been submitted successfully from your account" , "INFO", null, null);
//        log.info("Successfully created ChangeRequest with ID: {} for mobile: {}", saved.getId(), requestDto.getMobileNumber());
//        RmEntity rm = externalServiceClient.findRm(entity.getAccountId());
//        if (rm==null) {
//            log.error("RM not found for account ID: {}", entity.getAccountId());
//            notificationClientService.sendUnicast("01700000021", "Service Request ", "A service request has been submitted from your client " + entity.getInvestorCode(), "INFO", null, null);
//
//        }
//        else {
//            log.info("RM found for account ID: {} with name: {}", entity.getAccountId(), rm.getEmployeeName());
//            notificationClientService.sendUnicast(rm.getMobileNumber(), "Service Request ", String.format(
//                    "A service request has been submitted of a client %s",
//                    entity.getInvestorCode()
//            ), "INFO", null, null);
//        }
//
//        notificationClientService.sendUnicast("01921008451", "Service Request", String.format(
//                "A service request from client %s has been received",
//                entity.getInvestorCode()), "APPROVAL", null, null);
//
//        return new ServiceResponse(false, "Service request submitted for approval.", saved.getId(), "200");
//    }
//
//    @Override
//    public ServiceResponse getPreviousServiceRequests(String mobileNumber) throws JsonProcessingException {
//        log.info("Fetching previous service requests for mobile: {}", mobileNumber);
//        List<ChangeRequestEntity> changeRequestEntities = changeRequestRepository.findByMobileNumberOrderByRequestedAtDesc(mobileNumber);
//        return new ServiceResponse(false, "Successfully fetched previous service requests.", objectMapper.writeValueAsString(changeRequestEntities), "200");
//    }
//
//    @Override
//    public ServiceResponse approveRequest(String requestId) {
//        log.info("Approving change request with ID: {}", requestId);
//        ChangeRequestEntity request = changeRequestRepository.findById(requestId)
//                .orElseThrow(() -> new IllegalArgumentException("Change request not found with ID: " + requestId));
//        request.setStatus("APPROVED");
//        request.setCompletedAt(LocalDateTime.now());
//        changeRequestRepository.save(request);
//        return new ServiceResponse(false, "Request approved successfully.", request, "200");
//    }
//
//    @Override
//    public ServiceResponse changeMobileNumber(ChangeMobileRequestDto changeMobileRequestDto) {
//        return new ServiceResponse("Deprecated endpoint. Please use /service-request/submit instead.", "400");
//    }
//
//    @Override
//    public ServiceResponse changeEmail(ChangeEmailRequestDto changeEmailRequestDto) {
//        return new ServiceResponse("Deprecated endpoint. Please use /service-request/submit instead.", "400");
//    }
//
//    @Override
//    public ServiceResponse changeBankAccount(ChangeBankRequestDto changeBankRequestDto, MultipartFile chequeLeaf) throws IOException {
//        return new ServiceResponse("Deprecated endpoint. Please use /service-request/submit instead.", "400");
//    }
//
//    private void validateRootFields(GenericServiceRequestDto dto, List<String> requiredFields) {
//        for (String field : requiredFields) {
//            switch (field) {
//                case "mobileNumber" -> { if (dto.getMobileNumber() == null || dto.getMobileNumber().isEmpty()) throw new IllegalArgumentException("mobileNumber is required"); }
//                case "email" -> { if (dto.getEmail() == null || dto.getEmail().isEmpty()) throw new IllegalArgumentException("email is required"); }
//                case "otp" -> { if (dto.getOtp() == null || dto.getOtp().isEmpty()) throw new IllegalArgumentException("otp is required"); }
//                case "request", "fieldValues" -> {
//                    if ((dto.getRequest() == null || dto.getRequest().isEmpty()) && (dto.getFieldValues() == null || dto.getFieldValues().isEmpty())) {
//                        throw new IllegalArgumentException("request list or fieldValues cannot be empty");
//                    }
//                }
//            }
//        }
//    }
//
//    private void validateRequestPayload(List<Request> requestList, List<FormField> expectedFormFields) {
//        if (requestList == null || requestList.isEmpty()) {
//            return;
//        }
//
//        List<String> allowedParameters = expectedFormFields.stream()
//                .map(FormField::getParameterName)
//                .filter(Objects::nonNull)
//                .toList();
//
//        for (Request item : requestList) {
//            if (item.getRequestedValue() == null || item.getRequestedValue().isEmpty()) {
//                throw new IllegalArgumentException("Requested new value is missing for field: " + item.getLabel());
//            }
//
//            boolean isValidParam = item.getRequestedValue().keySet().stream()
//                    .anyMatch(allowedParameters::contains);
//
//            if (!isValidParam && !allowedParameters.isEmpty()) {
//                log.warn("Parameter {} sent for request field {} not found in allowed metadata parameters: {}",
//                        item.getRequestedValue().keySet(), item.getLabel(), allowedParameters);
//            }
//        }
//    }
//
//    public ServiceResponse submitTinChangeRequest(
//            TinChangeRequestDto dto) throws Exception {
//
//        log.info(
//                "Submitting TIN change request for account: {}",
//                dto.getAccountId()
//        );
//
//        // ---------------------------------------------------------
//        // 1. Validate OTP
//        // ---------------------------------------------------------
//
//        if (dto.getOtp() == null || dto.getOtp().isBlank()) {
//
//            String otpValue = String.format(
//                    "%06d",
//                    new java.security.SecureRandom().nextInt(999999)
//            );
//
//            log.info("OTP VALUE IS : {}", otpValue);
//
//            OtpVerification otpVerification =
//                    OtpVerification.builder()
//                            .identifier(dto.getMobileNumber())
//                            .otp(otpValue)
//                            .processName("TIN_CHANGE")
//                            .createdAt(new java.util.Date())
//                            .build();
//
//            otpRepository.save(otpVerification);
//
//            otpClient.sendOtp(
//                    dto.getMobileNumber(),
//                    dto.getEmail()
//            );
//
//            return new ServiceResponse(
//                    "OTP is required and was sent to mobile: "
//                            + dto.getMobileNumber()
//                            + " and email: "
//                            + dto.getEmail(),
//                    "427"
//            );
//        }
//
//
//        boolean otpValid =
//                otpClient.validateOtp(
//                        dto.getMobileNumber(),
//                        dto.getEmail(),
//                        dto.getOtp()
//                );
//
//        if (!otpValid) {
//            return new ServiceResponse(
//                    "OTP validation failed",
//                    "428"
//            );
//        }
//
//
//        // ---------------------------------------------------------
//        // 2. Validate input
//        // ---------------------------------------------------------
//
//        if (dto.getTinNumber() == null ||
//                dto.getTinNumber().isBlank()) {
//
//            return new ServiceResponse(
//                    "TIN certificate number is required",
//                    "400"
//            );
//        }
//
//        if (dto.getAction() == null ||
//                dto.getAction().isBlank()) {
//
//            dto.setAction("UPDATE");
//        }
//
//        String action = dto.getAction().toUpperCase();
//
//
//        // ---------------------------------------------------------
//        // 3. Upload certificate
//        // ---------------------------------------------------------
//
//        String certificateUrl = null;
//
//        if (dto.getTinCertificate() != null &&
//                !dto.getTinCertificate().isEmpty()) {
//
//            certificateUrl =
//                    minioService.upload(
//                            dto.getTinCertificate(), dto.getAccountId(),
//
//                            "tin-certificate-" + dto.getAccountId()
//                    );
//        }
//
//
//        // ---------------------------------------------------------
//        // 4. Build requested values
//        // ---------------------------------------------------------
//
//        Map<String, Object> requestedValue =
//                new HashMap<>();
//
//        requestedValue.put(
//                "tinNumber",
//                dto.getTinNumber()
//        );
//
//        if (certificateUrl != null) {
//            requestedValue.put(
//                    "tinCertificate",
//                    certificateUrl
//            );
//        }
//
//
//        // ---------------------------------------------------------
//        // 5. Build Request
//        // ---------------------------------------------------------
//
//        Request request =
//                Request.builder()
//                        .label("TIN_INFORMATION")
//                        .action(action)
//                        .requestedValue(requestedValue)
//                        .oldValues(Collections.emptyMap())
//                        .build();
//
//
//        // ---------------------------------------------------------
//        // 6. Create ChangeRequest
//        // ---------------------------------------------------------
//
//        ChangeRequestEntity entity =
//                ChangeRequestEntity.builder()
//                        .mobileNumber(dto.getMobileNumber())
//                        .accountId(dto.getAccountId())
//                        .email(dto.getEmail())
//
//                        .requestedBy(
//                                dto.getEmail() != null
//                                        ? dto.getEmail()
//                                        : dto.getMobileNumber()
//                        )
//
//                        .requestedFor("TIN_INFORMATION")
//                        .action(action)
//
//                        .request(
//                                Collections.singletonList(request)
//                        )
//
//                        .status("PENDING")
//
//                        .requestedAt(
//                                LocalDateTime.now()
//                        )
//
//                        .userRemarks(
//                                "TIN information change request submitted"
//                        )
//
//                        .userNotified(false)
//
//                        .build();
//
//
//        ChangeRequestEntity saved =
//                changeRequestRepository.save(entity);
//
//
//        // ---------------------------------------------------------
//        // 7. Notify RM
//        // ---------------------------------------------------------
//
//        RmEntity rm =
//                externalServiceClient.findRm(
//                        entity.getAccountId()
//                );
//
//        if (rm != null) {
//
//            notificationClientService.sendUnicast(
//                    rm.getMobileNumber(),
//                    "Service Request Submitted",
//                    "Dear " + rm.getEmployeeName()
//                            + ", a TIN information change request "
//                            + "has been submitted for account ID: "
//                            + entity.getAccountId(),
//                    "INFO",
//                    null,
//                    null
//            );
//        }
//
//
//        // ---------------------------------------------------------
//        // 8. Notify employee/admin
//        // ---------------------------------------------------------
//
//        notificationClientService.sendUnicast(
//                "01921008451",
//                "Service Request Submitted",
//                "A TIN information change request has been "
//                        + "submitted for account ID: "
//                        + entity.getAccountId(),
//                "APPROVAL",
//                null,
//                null
//        );
//
//
//        return new ServiceResponse(
//                false,
//                "TIN information submitted for approval.",
//                saved.getId(),
//                "200"
//        );
//    }
//
//    public ServiceResponse submitNomineeChangeRequest(
//            NomineeChangeRequestDto dto) throws Exception {
//
//        log.info(
//                "Submitting nominee change request for account: {}",
//                dto.getAccountId()
//        );
//
//
//        // =========================================================
//        // 1. OTP
//        // =========================================================
//
//        if (dto.getOtp() == null || dto.getOtp().isBlank()) {
//
//            String otpValue = String.format(
//                    "%06d",
//                    new java.security.SecureRandom().nextInt(999999)
//            );
//
//            log.info("OTP VALUE IS : {}", otpValue);
//
//            OtpVerification otpVerification =
//                    OtpVerification.builder()
//                            .identifier(dto.getMobileNumber())
//                            .otp(otpValue)
//                            .processName("NOMINEE_CHANGE")
//                            .createdAt(new java.util.Date())
//                            .build();
//
//            otpRepository.save(otpVerification);
//
//            otpClient.sendOtp(
//                    dto.getMobileNumber(),
//                    dto.getEmail()
//            );
//
//            return new ServiceResponse(
//                    "OTP is required and was sent to mobile: "
//                            + dto.getMobileNumber()
//                            + " and email: "
//                            + dto.getEmail(),
//                    "427"
//            );
//        }
//
//
//        boolean otpValid =
//                otpClient.validateOtp(
//                        dto.getMobileNumber(),
//                        dto.getEmail(),
//                        dto.getOtp()
//                );
//
//        if (!otpValid) {
//
//            return new ServiceResponse(
//                    "OTP validation failed",
//                    "428"
//            );
//        }
//
//
//        // =========================================================
//        // 2. Default action
//        // =========================================================
//
//        String action =
//                dto.getAction() == null ||
//                        dto.getAction().isBlank()
//                        ? "UPDATE"
//                        : dto.getAction().toUpperCase();
//
//
//        // =========================================================
//        // 3. Requested nominee values
//        // =========================================================
//
//        Map<String, Object> requestedValue =
//                new LinkedHashMap<>();
//
//        putIfNotNull(
//                requestedValue,
//                "name",
//                dto.getName()
//        );
//
//        putIfNotNull(
//                requestedValue,
//                "relation",
//                dto.getRelation()
//        );
//
//        putIfNotNull(
//                requestedValue,
//                "nid",
//                dto.getNid()
//        );
//
//        putIfNotNull(
//                requestedValue,
//                "nomineeDob",
//                dto.getNomineeDob()
//        );
//
//        putIfNotNull(
//                requestedValue,
//                "percentage",
//                dto.getPercentage()
//        );
//
//        putIfNotNull(
//                requestedValue,
//                "city",
//                dto.getCity()
//        );
//
//        putIfNotNull(
//                requestedValue,
//                "country",
//                dto.getCountry()
//        );
//
//        putIfNotNull(
//                requestedValue,
//                "state",
//                dto.getState()
//        );
//
//        putIfNotNull(
//                requestedValue,
//                "zipCode",
//                dto.getZipCode()
//        );
//
//        putIfNotNull(
//                requestedValue,
//                "address",
//                dto.getAddress()
//        );
//
//        putIfNotNull(
//                requestedValue,
//                "mobileNumber",
//                dto.getNomineeMobileNumber()
//        );
//
//        putIfNotNull(
//                requestedValue,
//                "residency",
//                dto.getResidency()
//        );
//
//        putIfNotNull(
//                requestedValue,
//                "minor",
//                dto.getMinor()
//        );
//
//
//        // =========================================================
//        // 4. Guardian information
//        // =========================================================
//
//        putIfNotNull(
//                requestedValue,
//                "guardianName",
//                dto.getGuardianName()
//        );
//
//        putIfNotNull(
//                requestedValue,
//                "relationshipWithNominee",
//                dto.getRelationshipWithNominee()
//        );
//
//        putIfNotNull(
//                requestedValue,
//                "guardianNidNumber",
//                dto.getGuardianNidNumber()
//        );
//
//
//        // =========================================================
//// Upload nominee files
//// =========================================================
//
//        uploadIfPresent(
//                requestedValue,
//                "nomineeNidFront",
//                dto.getNomineeNidFront(),
//                dto.getAccountId(),
//                "nominee-nid-front-" + dto.getAccountId()
//        );
//
//        uploadIfPresent(
//                requestedValue,
//                "nomineeNidBack",
//                dto.getNomineeNidBack(),
//                dto.getAccountId(),
//                "nominee-nid-back-" + dto.getAccountId()
//        );
//
//        uploadIfPresent(
//                requestedValue,
//                "nomineePhoto",
//                dto.getNomineePhoto(),
//                dto.getAccountId(),
//                "nominee-photo-" + dto.getAccountId()
//        );
//
//        uploadIfPresent(
//                requestedValue,
//                "nomineeSignature",
//                dto.getNomineeSignature(),
//                dto.getAccountId(),
//                "nominee-signature-" + dto.getAccountId()
//        );
//
//
//// =========================================================
//// Upload guardian files
//// =========================================================
//
//        uploadIfPresent(
//                requestedValue,
//                "guardianNidFront",
//                dto.getGuardianNidFront(),
//                dto.getAccountId(),
//                "guardian-nid-front-" + dto.getAccountId()
//        );
//
//        uploadIfPresent(
//                requestedValue,
//                "guardianNidBack",
//                dto.getGuardianNidBack(),
//                dto.getAccountId(),
//                "guardian-nid-back-" + dto.getAccountId()
//        );
//
//        uploadIfPresent(
//                requestedValue,
//                "guardianSignature",
//                dto.getGuardianSignature(),
//                dto.getAccountId(),
//                "guardian-signature-" + dto.getAccountId()
//        );
//
//
//        // =========================================================
//        // 7. Build Request
//        // =========================================================
//
//        Request request =
//                Request.builder()
//                        .label("NOMINEE_INFORMATION")
//                        .action(action)
//                        .requestedValue(requestedValue)
//                        .oldValues(Collections.emptyMap())
//                        .build();
//
//
//        // =========================================================
//        // 8. ChangeRequestEntity
//        // =========================================================
//
//        ChangeRequestEntity entity =
//                ChangeRequestEntity.builder()
//                        .mobileNumber(dto.getMobileNumber())
//                        .accountId(dto.getAccountId())
//                        .email(dto.getEmail())
//
//                        .requestedBy(
//                                dto.getEmail() != null
//                                        ? dto.getEmail()
//                                        : dto.getMobileNumber()
//                        )
//
//                        .requestedFor("NOMINEE_INFORMATION")
//                        .action(action)
//
//                        .request(
//                                Collections.singletonList(request)
//                        )
//
//                        .status("PENDING")
//
//                        .requestedAt(
//                                LocalDateTime.now()
//                        )
//
//                        .userRemarks(
//                                "Nominee information change request submitted"
//                        )
//
//                        .userNotified(false)
//
//                        .build();
//
//
//        ChangeRequestEntity saved =
//                changeRequestRepository.save(entity);
//
//
//        // =========================================================
//        // 9. RM notification
//        // =========================================================
//
//        RmEntity rm =
//                externalServiceClient.findRm(
//                        entity.getAccountId()
//                );
//
//        if (rm != null) {
//
//            notificationClientService.sendUnicast(
//                    rm.getMobileNumber(),
//                    "Service Request Submitted",
//                    "Dear " + rm.getEmployeeName()
//                            + ", a nominee information change request "
//                            + "has been submitted for account ID: "
//                            + entity.getAccountId(),
//                    "INFO",
//                    null,
//                    null
//            );
//        }
//
//
//        // =========================================================
//        // 10. Employee notification
//        // =========================================================
//
//        notificationClientService.sendUnicast(
//                "01921008451",
//                "Service Request Submitted",
//                "A nominee information change request has been "
//                        + "submitted for account ID: "
//                        + entity.getAccountId(),
//                "APPROVAL",
//                null,
//                null
//        );
//
//
//        return new ServiceResponse(
//                false,
//                "Nominee information submitted for approval.",
//                saved.getId(),
//                "200"
//        );
//    }
//
//    private void putIfNotNull(
//            Map<String, Object> map,
//            String key,
//            Object value) {
//
//        if (value != null) {
//            map.put(key, value);
//        }
//    }
//    private void uploadIfPresent(
//            Map<String, Object> map,
//            String key,
//            MultipartFile file,
//            String accountId,
//            String fileName) throws Exception {
//
//        if (file != null && !file.isEmpty()) {
//
//            String uploadedFile = minioService.upload(
//                    file,
//                    accountId,
//                    fileName
//            );
//
//            map.put(key, uploadedFile);
//        }
//    }
//}
