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
import com.beacepl.service_request_service.service.AccountSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

        // 2. Re-fetch current account from local AccountSnapshotService
        AccountSnapshot currentAccount = accountSnapshotService.getAccountById(entity.getAccountId());

        // 3. Concurrency check: verify old values have not changed since request creation
        verifyAccountHasNotChanged(entity, currentAccount);

        // 4. Apply changes directly to local AccountEntity in MongoDB
        accountSnapshotService.applyServiceRequest(entity);

        // 5. Update Status to APPROVED
        entity.setStatus(ServiceRequestStatus.APPROVED);
        entity.setReviewedBy(approvalDto != null ? approvalDto.getReviewedBy() : "ADMIN");
        entity.setReviewedAt(LocalDateTime.now());
        entity.setAdminRemark(approvalDto != null ? approvalDto.getAdminRemark() : "Approved by administrator");
        entity.setUpdatedAt(LocalDateTime.now());

        ServiceRequestEntity savedEntity = requestRepository.save(entity);
        log.info("Successfully approved service request ID: {} for account: {}", id, entity.getAccountId());

        // Audit Record
        auditRepository.save(ServiceRequestAuditEntity.builder()
                .requestId(savedEntity.getId())
                .serviceName(savedEntity.getServiceName())
                .accountId(savedEntity.getAccountId())
                .investorCode(savedEntity.getInvestorCode())
                .action("APPROVE")
                .status(ServiceRequestStatus.APPROVED.name())
                .performedBy(savedEntity.getReviewedBy())
                .remark(savedEntity.getAdminRemark())
                .timestamp(LocalDateTime.now())
                .build());

        serviceRequestNotificationUtil.sendNotification(savedEntity, savedEntity.getStatus());

        return ServiceResponse.success("Service request approved successfully", mapToAdminResponseDto(savedEntity));
    }

    public ServiceResponse<AdminServiceRequestResponseDto> rejectRequest(String id, RejectionRequestDto rejectionDto) {
        ServiceRequestEntity entity = requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Service request not found with ID: " + id));

        if (entity.getStatus() != ServiceRequestStatus.PENDING) {
            throw new InvalidRequestException("Cannot reject request. Current status is " + entity.getStatus());
        }

        entity.setStatus(ServiceRequestStatus.REJECTED);
        entity.setReviewedBy(rejectionDto != null ? rejectionDto.getReviewedBy() : "ADMIN");
        entity.setReviewedAt(LocalDateTime.now());
        entity.setAdminRemark(rejectionDto != null ? rejectionDto.getAdminRemark() : "Rejected by administrator");
        entity.setUpdatedAt(LocalDateTime.now());

        ServiceRequestEntity savedEntity = requestRepository.save(entity);
        log.info("Service request ID: {} rejected by admin. Reason: {}", id, savedEntity.getAdminRemark());

        // Audit Record
        auditRepository.save(ServiceRequestAuditEntity.builder()
                .requestId(savedEntity.getId())
                .serviceName(savedEntity.getServiceName())
                .accountId(savedEntity.getAccountId())
                .investorCode(savedEntity.getInvestorCode())
                .action("REJECT")
                .status(ServiceRequestStatus.REJECTED.name())
                .performedBy(savedEntity.getReviewedBy())
                .remark(savedEntity.getAdminRemark())
                .timestamp(LocalDateTime.now())
                .build());

        return ServiceResponse.success("Service request rejected successfully", mapToAdminResponseDto(savedEntity));
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
