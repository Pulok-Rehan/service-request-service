package com.beacepl.service_request_service.model;

import com.beacepl.service_request_service.enums.RequestAction;
import com.beacepl.service_request_service.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminChangeRequestDto {

    private String id;

    private String accountId;

    private String mobileNumber;

    private String investorCode;

    private String serviceName;

    private String displayName;

    private RequestAction action;

    private Map<String, Object> fieldValues;
    Map<String, String> fieldTypes;
    private Map<String, Object> oldValues;

    private String listItemIdentifierValue;

    private RequestStatus status;

    private String adminRemarks;

    private String reviewedBy;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}