package com.beacepl.service_request_service.service.impl;

import com.beacepl.service_request_service.client.OnboardingServiceClient;
import com.beacepl.service_request_service.entity.FieldConfigEntity;
import com.beacepl.service_request_service.entity.ServiceRequestAuditEntity;
import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.entity.ServiceRequestEntity;
import com.beacepl.service_request_service.enums.ServiceRequestStatus;
import com.beacepl.service_request_service.exceptions.ConfigurationNotFoundException;
import com.beacepl.service_request_service.exceptions.DuplicateRequestException;
import com.beacepl.service_request_service.exceptions.InvalidRequestException;
import com.beacepl.service_request_service.exceptions.RequestNotFoundException;
import com.beacepl.service_request_service.model.AccountSnapshot;
import com.beacepl.service_request_service.model.ServiceRequestSubmitDto;
import com.beacepl.service_request_service.model.ServiceResponse;
import com.beacepl.service_request_service.repository.ServiceRequestAuditRepository;
import com.beacepl.service_request_service.repository.ServiceRequestConfigRepository;
import com.beacepl.service_request_service.repository.ServiceRequestRepository;
import com.beacepl.service_request_service.service.handler.ServiceRequestHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
    private final OnboardingServiceClient onboardingServiceClient;
    private final FieldValidationService validationService;
    private final MinioService minioService;
    private final List<ServiceRequestHandler> requestHandlers;

    public ServiceResponse<ServiceRequestEntity> submitServiceRequest(
            ServiceRequestSubmitDto submitDto,
            Map<String, MultipartFile> uploadedFiles
    ) throws Exception {
        if (submitDto == null || submitDto.getServiceName() == null || submitDto.getServiceName().isBlank()) {
            throw new InvalidRequestException("serviceName is required");
        }

        String serviceName = submitDto.getServiceName();
        log.info("Processing service request submission for service: {}", serviceName);

        // 1. Retrieve Service Configuration
        ServiceRequestConfigEntity config = configRepository.findByServiceNameAndActiveTrue(serviceName)
                .orElseThrow(() -> new ConfigurationNotFoundException("Active service configuration not found for service: " + serviceName));

        // 2. Account Identification & Validation
        String accountIdentifier = getAccountIdentifier(submitDto);
        AccountSnapshot account = onboardingServiceClient.getAccountById(accountIdentifier);
        if (account == null) {
            account = onboardingServiceClient.searchAccount(accountIdentifier);
        }

        String accountId = (account.getId() != null) ? account.getId() : submitDto.getAccountId();
        String investorCode = (account.getInvestorCode() != null) ? account.getInvestorCode() : submitDto.getInvestorCode();
        String mobileNumber = (account.getMobileNumber() != null) ? account.getMobileNumber() : submitDto.getMobileNumber();
        String email = (account.getEmailAddress() != null) ? account.getEmailAddress() : submitDto.getEmail();

        // 3. Duplicate Pending Request Protection (Requirement 37)
        if (!"NOMINEE_ADD".equalsIgnoreCase(serviceName)) {
            boolean duplicateExists = requestRepository.existsByAccountIdAndServiceNameAndStatus(
                    accountId, serviceName, ServiceRequestStatus.PENDING
            );
            if (duplicateExists) {
                throw new DuplicateRequestException("A pending request for service '" + config.getDisplayName() + "' already exists for this account.");
            }
        }

        // 4. Validate Action
        String action = (submitDto.getAction() != null && !submitDto.getAction().isBlank())
                ? submitDto.getAction().toUpperCase()
                : (config.getAllowedActions() != null && !config.getAllowedActions().isEmpty() ? config.getAllowedActions().get(0) : "EDIT");

        // 5. Dynamic Field Validation
        Map<String, Object> submittedFields = submitDto.getFieldValues() != null ? submitDto.getFieldValues() : new HashMap<>();
        validationService.validateServiceAndFields(
                config, action, submitDto.getListItemIdentifierValue(), submittedFields, uploadedFiles
        );

        // 6. Handle File Uploads to MinIO
        String generateId = "SR-" + UUID.randomUUID().toString().substring(0, 8);
        Map<String, String> uploadedFileNames = new HashMap<>();

        if (config.getFields() != null && uploadedFiles != null && !uploadedFiles.isEmpty()) {
            for (FieldConfigEntity fieldConfig : config.getFields()) {
                if (fieldConfig.isFile() || fieldConfig.getDataType().name().equals("FILE")) {
                    MultipartFile file = uploadedFiles.get(fieldConfig.getFieldName());
                    if (file != null && !file.isEmpty()) {
                        String objectName = minioService.upload(
                                file, investorCode, generateId, fieldConfig.getFieldName()
                        );
                        uploadedFileNames.put(fieldConfig.getFieldName(), objectName);
                    }
                }
            }
        }

        // 7. Resolve Strategy Handler for Old / New Values
        ServiceRequestHandler handler = requestHandlers.stream()
                .filter(h -> h.supports(config))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No request handler found for service config: " + serviceName));

        Map<String, Object> oldValues = handler.resolveOldValues(
                account, config, submitDto.getListItemIdentifierValue(), submittedFields
        );
        Map<String, Object> newValues = handler.resolveNewValues(
                submittedFields, uploadedFileNames, config
        );
        List<com.beacepl.service_request_service.model.FieldChangeDetail> fieldDetails = handler.buildFieldDetails(
                oldValues, newValues, config
        );

        // 8. Build & Save Service Request Entity
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
                .listItemIdentifierValue(submitDto.getListItemIdentifierValue())
                .status(ServiceRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ServiceRequestEntity savedEntity = requestRepository.save(entity);
        log.info("Service request created successfully with ID: {} for account: {}", savedEntity.getId(), accountId);

        // 9. Save Audit Information
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
                .build()
        );

        return ServiceResponse.success("Service request submitted successfully.", savedEntity);
    }

    public ServiceResponse<Page<ServiceRequestEntity>> getUserRequests(
            String identifier,
            ServiceRequestStatus status,
            String serviceName,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ServiceRequestEntity> requests;

        if (status != null) {
            requests = requestRepository.findByAccountIdAndStatus(identifier, status, pageable);
        } else {
            requests = requestRepository.findByAccountId(identifier, pageable);
            if (requests.isEmpty()) {
                requests = requestRepository.findByMobileNumberOrEmailOrInvestorCodeOrAccountId(
                        identifier, identifier, identifier, identifier, pageable
                );
            }
        }

        return ServiceResponse.success("User request history fetched successfully", requests);
    }

    public ServiceResponse<ServiceRequestEntity> getRequestById(String id) {
        ServiceRequestEntity entity = requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Service request not found with ID: " + id));
        return ServiceResponse.success(entity);
    }

    private String getAccountIdentifier(ServiceRequestSubmitDto dto) {
        if (dto.getAccountId() != null && !dto.getAccountId().isBlank()) return dto.getAccountId();
        if (dto.getInvestorCode() != null && !dto.getInvestorCode().isBlank()) return dto.getInvestorCode();
        if (dto.getMobileNumber() != null && !dto.getMobileNumber().isBlank()) return dto.getMobileNumber();
        if (dto.getEmailAddress() != null && !dto.getEmailAddress().isBlank()) return dto.getEmailAddress();
        throw new InvalidRequestException("Account identifier (accountId, investorCode, mobileNumber, or email) is required.");
    }
}
