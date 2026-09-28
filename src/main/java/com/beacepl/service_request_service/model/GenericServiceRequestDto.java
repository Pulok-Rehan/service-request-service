package com.beacepl.service_request_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericServiceRequestDto {
    private String accountId;
    private String email;
    private String mobileNumber;
    private String investorCode;
    private String serviceName; // e.g., "Address", "Bank Information", "Mobile Number"
    private String action; // e.g., "ADD", "UPDATE"
    private String otp;

    // Dynamic list of request fields (old vs requested values)
    private List<Request> request;

    // Fallback/direct key-value payload map
    private Map<String, Object> fieldValues;
}