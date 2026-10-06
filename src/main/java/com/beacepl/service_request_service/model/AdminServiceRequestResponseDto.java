package com.beacepl.service_request_service.model;

import com.beacepl.service_request_service.entity.ApprovalHistoryItem;
import com.beacepl.service_request_service.entity.DownstreamExecutionInfo;
import com.beacepl.service_request_service.enums.ServiceRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminServiceRequestResponseDto {

    private String id;

    private String accountId;

    private String mobileNumber;

    private String investorCode;

    private String email;

    private String serviceName;

    private String displayName;

    private String section;

    private String action;

    private Map<String, Object> oldValues;

    private Map<String, Object> newValues;

    /** Rich list of field details for admin UI rendering (label, fieldName, type, oldValue, newValue) */
    private List<FieldChangeDetail> fieldDetails;

    private String listItemIdentifierValue;

    private ServiceRequestStatus status;

    private Integer currentLevel;

    private Integer totalLevels;

    private List<ApprovalHistoryItem> approvalHistory;

    private DownstreamExecutionInfo downstreamExecution;

    private String adminRemark;

    private String reviewedBy;

    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
