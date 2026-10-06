package com.beacepl.service_request_service.service.impl;

import com.beacepl.service_request_service.ServiceRequestNotificationUtil;
import com.beacepl.service_request_service.client.OnboardingServiceClient;
import com.beacepl.service_request_service.entity.ServiceRequestAuditEntity;
import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.entity.ServiceRequestEntity;
import com.beacepl.service_request_service.enums.ServiceRequestStatus;
import com.beacepl.service_request_service.exceptions.AccountConflictException;
import com.beacepl.service_request_service.exceptions.InvalidRequestException;
import com.beacepl.service_request_service.exceptions.RequestNotFoundException;
import com.beacepl.service_request_service.model.AccountSnapshot;
import com.beacepl.service_request_service.model.AdminServiceRequestResponseDto;
import com.beacepl.service_request_service.model.ApprovalRequestDto;
import com.beacepl.service_request_service.model.RejectionRequestDto;
import com.beacepl.service_request_service.model.ServiceResponse;
import com.beacepl.service_request_service.repository.ServiceRequestAuditRepository;
import com.beacepl.service_request_service.repository.ServiceRequestConfigRepository;
import com.beacepl.service_request_service.repository.ServiceRequestRepository;
import com.beacepl.service_request_service.MinioUtil;
import com.beacepl.service_request_service.entity.ApprovalHistoryItem;
import com.beacepl.service_request_service.service.AccountSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceRequestServiceImpl {

    private final ServiceRequestRepository requestRepository;
    private final ServiceRequestConfigRepository configRepository;
    private final ServiceRequestAuditRepository auditRepository;
    private final OnboardingServiceClient onboardingServiceClient;
    private final MinioService minioService;
    private final OldValueResolver oldValueResolver;
    private final AccountSnapshotService accountSnapshotService;
    private final ServiceRequestNotificationUtil serviceRequestNotificationUtil;
    private final DownstreamApiDispatcherService downstreamApiDispatcherService;

    public ServiceResponse<List<AdminServiceRequestResponseDto>> listAdminRequests(
            ServiceRequestStatus status,
            String serviceName,
            String investorCode,
            String accountId,
            String mobileNumber,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ServiceRequestEntity> entityPage;

        if (status != null) {
            entityPage = requestRepository.findAdminRequests(
                    status,
                    cleanQuery(serviceName),
                    cleanQuery(investorCode),
                    cleanQuery(accountId),
                    cleanQuery(mobileNumber),
                    pageable);
        } else {
            entityPage = requestRepository.findAll(pageable);
        }

        List<AdminServiceRequestResponseDto> dtos = entityPage.getContent().stream()
                .map(this::mapToAdminResponseDto)
                .toList();

        return ServiceResponse.success("Admin request list fetched successfully", dtos);
    }

    public ServiceResponse<AdminServiceRequestResponseDto> getRequestDetail(String id) {
        ServiceRequestEntity entity = requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Service request not found with ID: " + id));

        AdminServiceRequestResponseDto responseDto = mapToAdminResponseDto(entity);
        return ServiceResponse.success("Request detail fetched successfully", responseDto);
    }

    public ServiceResponse<AdminServiceRequestResponseDto> approveRequest(String id, ApprovalRequestDto approvalDto) {
        ServiceRequestEntity entity = requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Service request not found with ID: " + id));

        // 1. Validate PENDING status
        if (entity.getStatus() != ServiceRequestStatus.PENDING) {
            throw new InvalidRequestException("Cannot approve request. Current status is " + entity.getStatus());
        }

        ServiceRequestConfigEntity config = configRepository.findByServiceName(entity.getServiceName()).orElse(null);

        int currentLevel = entity.getCurrentLevel() != null ? entity.getCurrentLevel() : 1;
        int totalLevels = entity.getTotalLevels() != null ? entity.getTotalLevels() : 1;

        // Resolve level name from config if configured
        String levelName = "Level " + currentLevel;
        if (config != null && config.getApprovalLevels() != null && !config.getApprovalLevels().isEmpty()) {
            for (var lvl : config.getApprovalLevels()) {
                if (lvl.getLevel() != null && lvl.getLevel() == currentLevel && lvl.getLevelName() != null) {
                    levelName = lvl.getLevelName();
                    break;
                }
            }
        }

        String approver = (approvalDto != null && approvalDto.getReviewedBy() != null) ? approvalDto.getReviewedBy() : "ADMIN";
        String approverRole = (approvalDto != null && approvalDto.getRole() != null) ? approvalDto.getRole() : "ADMIN";
        String remark = (approvalDto != null && approvalDto.getAdminRemark() != null) ? approvalDto.getAdminRemark() : "Approved";

        // Record level approval in history
        ApprovalHistoryItem historyItem = ApprovalHistoryItem.builder()
                .level(currentLevel)
                .levelName(levelName)
                .action("APPROVED")
                .actionBy(approver)
                .role(approverRole)
                .remark(remark)
                .timestamp(LocalDateTime.now())
                .build();

        if (entity.getApprovalHistory() == null) {
            entity.setApprovalHistory(new ArrayList<>());
        }
        entity.getApprovalHistory().add(historyItem);

        // Check if there are further approval levels
        if (currentLevel < totalLevels) {
            // Advance to next level
            int nextLevel = currentLevel + 1;
            entity.setCurrentLevel(nextLevel);
            entity.setReviewedBy(approver);
            entity.setReviewedAt(LocalDateTime.now());
            entity.setAdminRemark("Level " + currentLevel + " approved by " + approver + ". Pending Level " + nextLevel);
            entity.setUpdatedAt(LocalDateTime.now());

            ServiceRequestEntity savedEntity = requestRepository.save(entity);
            log.info("Request ID: {} advanced to Level {} by {}", id, nextLevel, approver);

            auditRepository.save(ServiceRequestAuditEntity.builder()
                    .requestId(savedEntity.getId())
                    .serviceName(savedEntity.getServiceName())
                    .accountId(savedEntity.getAccountId())
                    .investorCode(savedEntity.getInvestorCode())
                    .action("APPROVE_LEVEL_" + currentLevel)
                    .status(savedEntity.getStatus().name())
                    .performedBy(approver)
                    .remark(savedEntity.getAdminRemark())
                    .timestamp(LocalDateTime.now())
                    .build());

            serviceRequestNotificationUtil.sendNotification(savedEntity, savedEntity.getStatus());
            return ServiceResponse.success("Level " + currentLevel + " approved. Request advanced to Level " + nextLevel, mapToAdminResponseDto(savedEntity));
        }

        // --- FINAL LEVEL APPROVAL ---
        // 2. Re-fetch current account from local AccountSnapshotService
        AccountSnapshot currentAccount = accountSnapshotService.getAccountById(entity.getAccountId());

        // 3. Concurrency check: verify old values have not changed since request creation
        verifyAccountHasNotChanged(entity, currentAccount);

        // 4. Apply changes directly to local AccountEntity in MongoDB
        accountSnapshotService.applyServiceRequest(entity);

        // 5. Update Status & final review details
        entity.setStatus(ServiceRequestStatus.APPROVED);
        entity.setReviewedBy(approver);
        entity.setReviewedAt(LocalDateTime.now());
        entity.setAdminRemark(remark);
        entity.setUpdatedAt(LocalDateTime.now());

        // 6. Trigger Downstream API Dispatch if configured
        if (config != null && config.getApiConfig() != null && config.getApiConfig().getTargetUrl() != null && !config.getApiConfig().getTargetUrl().isBlank()) {
            boolean dispatchOk = downstreamApiDispatcherService.dispatch(entity, config);
            if (dispatchOk) {
                entity.setStatus(ServiceRequestStatus.EXECUTED);
            } else {
                entity.setStatus(ServiceRequestStatus.EXECUTION_FAILED);
            }
        }

        ServiceRequestEntity savedEntity = requestRepository.save(entity);
        log.info("Final approval completed for request ID: {}. Final status: {}", id, savedEntity.getStatus());

        // Audit Record
        auditRepository.save(ServiceRequestAuditEntity.builder()
                .requestId(savedEntity.getId())
                .serviceName(savedEntity.getServiceName())
                .accountId(savedEntity.getAccountId())
                .investorCode(savedEntity.getInvestorCode())
                .action("FINAL_APPROVE")
                .status(savedEntity.getStatus().name())
                .performedBy(approver)
                .remark(savedEntity.getAdminRemark())
                .timestamp(LocalDateTime.now())
                .build());

        serviceRequestNotificationUtil.sendNotification(savedEntity, savedEntity.getStatus());

        return ServiceResponse.success("Service request fully approved with status: " + savedEntity.getStatus(), mapToAdminResponseDto(savedEntity));
    }

    public ServiceResponse<AdminServiceRequestResponseDto> rejectRequest(String id, RejectionRequestDto rejectionDto) {
        ServiceRequestEntity entity = requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Service request not found with ID: " + id));

        if (entity.getStatus() != ServiceRequestStatus.PENDING) {
            throw new InvalidRequestException("Cannot reject request. Current status is " + entity.getStatus());
        }

        int currentLevel = entity.getCurrentLevel() != null ? entity.getCurrentLevel() : 1;
        String approver = (rejectionDto != null && rejectionDto.getReviewedBy() != null) ? rejectionDto.getReviewedBy() : "ADMIN";
        String approverRole = (rejectionDto != null && rejectionDto.getRole() != null) ? rejectionDto.getRole() : "ADMIN";
        String remark = (rejectionDto != null && rejectionDto.getAdminRemark() != null) ? rejectionDto.getAdminRemark() : "Rejected by administrator";

        ApprovalHistoryItem historyItem = ApprovalHistoryItem.builder()
                .level(currentLevel)
                .levelName("Level " + currentLevel)
                .action("REJECTED")
                .actionBy(approver)
                .role(approverRole)
                .remark(remark)
                .timestamp(LocalDateTime.now())
                .build();

        if (entity.getApprovalHistory() == null) {
            entity.setApprovalHistory(new ArrayList<>());
        }
        entity.getApprovalHistory().add(historyItem);

        entity.setStatus(ServiceRequestStatus.REJECTED);
        entity.setReviewedBy(approver);
        entity.setReviewedAt(LocalDateTime.now());
        entity.setAdminRemark(remark);
        entity.setUpdatedAt(LocalDateTime.now());

        ServiceRequestEntity savedEntity = requestRepository.save(entity);
        log.info("Service request ID: {} rejected at Level {}. Reason: {}", id, currentLevel, remark);

        // Audit Record
        auditRepository.save(ServiceRequestAuditEntity.builder()
                .requestId(savedEntity.getId())
                .serviceName(savedEntity.getServiceName())
                .accountId(savedEntity.getAccountId())
                .investorCode(savedEntity.getInvestorCode())
                .action("REJECT_LEVEL_" + currentLevel)
                .status(ServiceRequestStatus.REJECTED.name())
                .performedBy(approver)
                .remark(remark)
                .timestamp(LocalDateTime.now())
                .build());

        serviceRequestNotificationUtil.sendNotification(savedEntity, savedEntity.getStatus());

        return ServiceResponse.success("Service request rejected successfully", mapToAdminResponseDto(savedEntity));
    }

    public ServiceResponse<AdminServiceRequestResponseDto> retryExecution(String id) {
        ServiceRequestEntity entity = requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Service request not found with ID: " + id));

        if (entity.getStatus() != ServiceRequestStatus.EXECUTION_FAILED && entity.getStatus() != ServiceRequestStatus.APPROVED) {
            throw new InvalidRequestException("Can only retry execution for requests in EXECUTION_FAILED or APPROVED status. Current status: " + entity.getStatus());
        }

        ServiceRequestConfigEntity config = configRepository.findByServiceName(entity.getServiceName()).orElse(null);
        boolean dispatchOk = downstreamApiDispatcherService.dispatch(entity, config);
        if (dispatchOk) {
            entity.setStatus(ServiceRequestStatus.EXECUTED);
        } else {
            entity.setStatus(ServiceRequestStatus.EXECUTION_FAILED);
        }
        entity.setUpdatedAt(LocalDateTime.now());
        ServiceRequestEntity saved = requestRepository.save(entity);

        auditRepository.save(ServiceRequestAuditEntity.builder()
                .requestId(saved.getId())
                .serviceName(saved.getServiceName())
                .accountId(saved.getAccountId())
                .investorCode(saved.getInvestorCode())
                .action("RETRY_EXECUTION")
                .status(saved.getStatus().name())
                .performedBy("ADMIN")
                .remark("Execution retry result: " + saved.getStatus())
                .timestamp(LocalDateTime.now())
                .build());

        return ServiceResponse.success("Downstream execution retried. Current status: " + saved.getStatus(), mapToAdminResponseDto(saved));
    }

    private void verifyAccountHasNotChanged(ServiceRequestEntity entity, AccountSnapshot currentAccount) {
        if (entity.getOldValues() == null || entity.getOldValues().isEmpty()) {
            return;
        }

        Optional<ServiceRequestConfigEntity> configOpt = configRepository.findByServiceName(entity.getServiceName());
        if (configOpt.isEmpty()) {
            return;
        }

        ServiceRequestConfigEntity config = configOpt.get();

        Map<String, Object> currentResolvedOldValues = oldValueResolver.resolveOldValues(
                currentAccount, config, entity.getListItemIdentifierValue(), entity.getNewValues());

        for (Map.Entry<String, Object> oldEntry : entity.getOldValues().entrySet()) {
            String field = oldEntry.getKey();
            Object expectedOldValue = oldEntry.getValue();
            Object currentActualValue = currentResolvedOldValues.get(field);

            if (expectedOldValue != null && currentActualValue != null) {
                if (!Objects.equals(expectedOldValue.toString(), currentActualValue.toString())) {
                    log.warn(
                            "Account concurrency conflict detected for request ID: {}. Field '{}' stored old value='{}', current account value='{}'",
                            entity.getId(), field, expectedOldValue, currentActualValue);
                    throw new AccountConflictException("ACCOUNT_CHANGED_SINCE_REQUEST: Field '" + field
                            + "' has been modified on the account since request submission.");
                }
            }
        }
    }

    private AdminServiceRequestResponseDto mapToAdminResponseDto(ServiceRequestEntity entity) {
        Map<String, Object> presignedOldValues = convertFileObjectNamesToPresignedUrls(entity.getOldValues());
        Map<String, Object> presignedNewValues = convertFileObjectNamesToPresignedUrls(entity.getNewValues());

        List<com.beacepl.service_request_service.model.FieldChangeDetail> presignedDetails = null;
        if (entity.getFieldDetails() != null) {
            presignedDetails = entity.getFieldDetails().stream()
                    .map(detail -> {
                        Object oldVal = detail.getOldValue();
                        Object newVal = detail.getNewValue();
                        if (detail.isFile() || "FILE".equalsIgnoreCase(detail.getDataType())) {
                            if (oldVal instanceof String s && isLikelyFileObjectName(s)) {
                                oldVal = MinioUtil.getImageUrl(s);
                            }
                            if (newVal instanceof String s && isLikelyFileObjectName(s)) {
                                newVal = MinioUtil.getImageUrl(s);
                            }
                        }
                        return com.beacepl.service_request_service.model.FieldChangeDetail.builder()
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

        return AdminServiceRequestResponseDto.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .mobileNumber(entity.getMobileNumber())
                .investorCode(entity.getInvestorCode())
                .email(entity.getEmail())
                .serviceName(entity.getServiceName())
                .displayName(entity.getDisplayName())
                .section(entity.getSection())
                .action(entity.getAction())
                .oldValues(presignedOldValues)
                .newValues(presignedNewValues)
                .fieldDetails(presignedDetails)
                .listItemIdentifierValue(entity.getListItemIdentifierValue())
                .status(entity.getStatus())
                .currentLevel(entity.getCurrentLevel())
                .totalLevels(entity.getTotalLevels())
                .approvalHistory(entity.getApprovalHistory())
                .downstreamExecution(entity.getDownstreamExecution())
                .adminRemark(entity.getAdminRemark())
                .reviewedBy(entity.getReviewedBy())
                .reviewedAt(entity.getReviewedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Map<String, Object> convertFileObjectNamesToPresignedUrls(Map<String, Object> values) {
        if (values == null)
            return null;
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof String strVal) {
                if (isLikelyFileObjectName(strVal)) {
                    result.put(entry.getKey(), MinioUtil.getImageUrl(strVal));
                    continue;
                }
            }
            result.put(entry.getKey(), val);
        }
        return result;
    }

    private boolean isLikelyFileObjectName(String value) {
        if (value == null) {
            return false;
        }

        String lower = value.toLowerCase();

        return lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".pdf")
                || lower.contains("tin_certificate")
                || lower.contains("-nominee-")
                || lower.contains("nid_front")
                || lower.contains("nid_back")
                || lower.contains("photo")
                || lower.contains("tin");
    }

    private String cleanQuery(String value) {
        return (value != null && !value.isBlank()) ? value : null;
    }
}
