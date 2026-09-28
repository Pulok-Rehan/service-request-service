package com.beacepl.service_request_service.model;

import lombok.Data;

import java.util.Map;

@Data
public class AdminActionDto {
    private String status;
    private String remarks;
    private String adminId;
    private Map<String, Object> fieldValues;
}
