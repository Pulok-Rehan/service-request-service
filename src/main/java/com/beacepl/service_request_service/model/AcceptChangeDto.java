package com.beacepl.service_request_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AcceptChangeDto {
    private String mobileNumber;
    private String accountId;
    private String serviceName;
    private String action;
    private Map<String, Object> fieldValues;

    public AcceptChangeDto(String mobileNumber, Map<String, Object> fieldValues) {
        this.mobileNumber = mobileNumber;
        this.fieldValues = fieldValues;
    }
}
