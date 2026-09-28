package com.beacepl.service_request_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request {
//    private String label;
//    private String action; // e.g., "ADD", "UPDATE"
//    private Map<String, Object> requestedValue;
    private Map<String, Object> oldValues;
}
