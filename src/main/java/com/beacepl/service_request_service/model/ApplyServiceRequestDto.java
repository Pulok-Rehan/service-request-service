package com.beacepl.service_request_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplyServiceRequestDto {
    private String accountId;
    private String serviceName;
    private String action;
    private Map<String, Object> expectedOldValues;
    private Map<String, Object> newValues;
    private String listItemIdentifierValue;
}
