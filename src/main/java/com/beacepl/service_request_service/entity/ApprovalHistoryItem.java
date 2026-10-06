package com.beacepl.service_request_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalHistoryItem {

    private Integer level;

    private String levelName;

    /** Action: APPROVED, REJECTED */
    private String action;

    private String actionBy;

    private String role;

    private String remark;

    private LocalDateTime timestamp;
}
