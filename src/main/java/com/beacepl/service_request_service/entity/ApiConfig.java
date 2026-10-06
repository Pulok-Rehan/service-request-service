package com.beacepl.service_request_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiConfig {

    private String targetUrl;

    /** HTTP method: GET, POST, PUT, PATCH, DELETE */
    private String httpMethod;

    /** Static or template headers, e.g. Content-Type, X-Service-Name */
    private Map<String, String> headers;

    /** Path variable keys, e.g. ["accountId"] */
    private List<String> pathParams;

    /** Query param mappings, e.g. {"investorCode": "${investorCode}"} */
    private Map<String, String> queryParams;

    /** Dynamic body payload template */
    private Map<String, Object> bodyTemplate;
}
