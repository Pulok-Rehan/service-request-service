package com.beacepl.service_request_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalLevelConfig {

    /** Sequential level number: 1, 2, 3, etc. */
    private Integer level;

    /** Human-readable name: e.g. "Branch RM Review", "Compliance Approval" */
    private String levelName;

    /** Roles allowed to approve at this level, e.g. ["ROLE_RM", "ROLE_BRANCH_MANAGER"] */
    private List<String> allowedRoles;

    /** Specific employee or user IDs allowed to approve at this level (optional) */
    private List<String> specificApproverIds;
}
