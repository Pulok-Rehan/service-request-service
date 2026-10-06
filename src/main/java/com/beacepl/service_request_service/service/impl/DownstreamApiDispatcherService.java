package com.beacepl.service_request_service.service.impl;

import com.beacepl.service_request_service.entity.ApiConfig;
import com.beacepl.service_request_service.entity.DownstreamExecutionInfo;
import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.entity.ServiceRequestEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DownstreamApiDispatcherService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Dispatches the HTTP request to the downstream target API configured in ServiceRequestConfig.
     *
     * @param request The approved ServiceRequestEntity
     * @param config The ServiceRequestConfigEntity
     * @return true if successful (or if no API configured), false on failure
     */
    public boolean dispatch(ServiceRequestEntity request, ServiceRequestConfigEntity config) {
        if (config == null || config.getApiConfig() == null) {
            log.info("No downstream API configured for service: {}. Skipping downstream dispatch.", request.getServiceName());
            return true;
        }

        ApiConfig apiConfig = config.getApiConfig();
        if (apiConfig.getTargetUrl() == null || apiConfig.getTargetUrl().isBlank()) {
            log.info("Target URL is blank for service: {}. Skipping downstream dispatch.", request.getServiceName());
            return true;
        }

        DownstreamExecutionInfo executionInfo = DownstreamExecutionInfo.builder()
                .executedAt(LocalDateTime.now())
                .httpMethod(apiConfig.getHttpMethod() != null ? apiConfig.getHttpMethod().toUpperCase() : "POST")
                .retryCount(request.getDownstreamExecution() != null ? request.getDownstreamExecution().getRetryCount() + 1 : 1)
                .build();

        try {
            // 1. Build Target URL with Path Parameters
            String resolvedUrl = resolveUrlPathParams(apiConfig.getTargetUrl(), request);

            // 2. Append Query Parameters if configured
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(resolvedUrl);
            if (apiConfig.getQueryParams() != null && !apiConfig.getQueryParams().isEmpty()) {
                for (Map.Entry<String, String> entry : apiConfig.getQueryParams().entrySet()) {
                    String val = resolvePlaceholder(entry.getValue(), request);
                    uriBuilder.queryParam(entry.getKey(), val);
                }
            }
            String finalUrl = uriBuilder.toUriString();
            executionInfo.setTargetUrl(finalUrl);

            // 3. Build Headers
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            if (apiConfig.getHeaders() != null) {
                for (Map.Entry<String, String> header : apiConfig.getHeaders().entrySet()) {
                    httpHeaders.set(header.getKey(), resolvePlaceholder(header.getValue(), request));
                }
            }

            // 4. Build Request Payload
            Object payload = buildPayload(apiConfig.getBodyTemplate(), request);
            String payloadJson = (payload != null) ? objectMapper.writeValueAsString(payload) : null;
            executionInfo.setRequestPayload(payloadJson);

            // 5. Determine HTTP Method
            HttpMethod httpMethod = HttpMethod.valueOf(
                    apiConfig.getHttpMethod() != null ? apiConfig.getHttpMethod().toUpperCase() : "POST"
            );

            HttpEntity<Object> httpEntity = new HttpEntity<>(payload, httpHeaders);

            log.info("Dispatching downstream API call for request {}: {} {}", request.getId(), httpMethod, finalUrl);

            // 6. Execute Request
            ResponseEntity<String> response = restTemplate.exchange(finalUrl, httpMethod, httpEntity, String.class);

            executionInfo.setHttpStatus(response.getStatusCode().value());
            executionInfo.setResponseBody(response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                executionInfo.setStatus("SUCCESS");
                request.setDownstreamExecution(executionInfo);
                log.info("Downstream API dispatch succeeded for request ID: {} with status: {}", request.getId(), response.getStatusCode());
                return true;
            } else {
                executionInfo.setStatus("FAILED");
                executionInfo.setErrorMessage("Downstream API returned non-2xx status: " + response.getStatusCode());
                request.setDownstreamExecution(executionInfo);
                log.warn("Downstream API returned non-2xx for request ID: {}: {}", request.getId(), response.getStatusCode());
                return false;
            }

        } catch (HttpStatusCodeException ex) {
            log.error("Downstream API HTTP error for request {}: status={}, body={}", request.getId(), ex.getStatusCode(), ex.getResponseBodyAsString());
            executionInfo.setHttpStatus(ex.getStatusCode().value());
            executionInfo.setResponseBody(ex.getResponseBodyAsString());
            executionInfo.setErrorMessage(ex.getMessage());
            executionInfo.setStatus("FAILED");
            request.setDownstreamExecution(executionInfo);
            return false;
        } catch (Exception ex) {
            log.error("Downstream API execution error for request {}: {}", request.getId(), ex.getMessage(), ex);
            executionInfo.setHttpStatus(500);
            executionInfo.setErrorMessage(ex.getMessage());
            executionInfo.setStatus("FAILED");
            request.setDownstreamExecution(executionInfo);
            return false;
        }
    }

    private String resolveUrlPathParams(String urlTemplate, ServiceRequestEntity request) {
        if (urlTemplate == null) return null;
        String resolved = urlTemplate;
        if (request.getAccountId() != null) {
            resolved = resolved.replace("{accountId}", request.getAccountId());
        }
        if (request.getInvestorCode() != null) {
            resolved = resolved.replace("{investorCode}", request.getInvestorCode());
        }
        if (request.getId() != null) {
            resolved = resolved.replace("{requestId}", request.getId());
        }
        if (request.getMobileNumber() != null) {
            resolved = resolved.replace("{mobileNumber}", request.getMobileNumber());
        }
        if (request.getEmail() != null) {
            resolved = resolved.replace("{email}", request.getEmail());
        }
        return resolved;
    }

    private Object buildPayload(Map<String, Object> template, ServiceRequestEntity request) {
        if (template == null || template.isEmpty()) {
            // Default to sending newValues directly if no template is defined
            return request.getNewValues() != null ? request.getNewValues() : new HashMap<>();
        }

        Map<String, Object> resolved = new HashMap<>();
        for (Map.Entry<String, Object> entry : template.entrySet()) {
            resolved.put(entry.getKey(), resolveTemplateValue(entry.getValue(), request));
        }
        return resolved;
    }

    private Object resolveTemplateValue(Object val, ServiceRequestEntity request) {
        if (val instanceof String strVal) {
            return resolvePlaceholder(strVal, request);
        }
        return val;
    }

    private String resolvePlaceholder(String text, ServiceRequestEntity request) {
        if (text == null) return null;

        Map<String, Object> newValues = request.getNewValues() != null ? request.getNewValues() : Map.of();

        String result = text;
        if (result.contains("${accountId}") && request.getAccountId() != null) {
            result = result.replace("${accountId}", request.getAccountId());
        }
        if (result.contains("${investorCode}") && request.getInvestorCode() != null) {
            result = result.replace("${investorCode}", request.getInvestorCode());
        }
        if (result.contains("${requestId}") && request.getId() != null) {
            result = result.replace("${requestId}", request.getId());
        }
        if (result.contains("${mobileNumber}") && request.getMobileNumber() != null) {
            result = result.replace("${mobileNumber}", request.getMobileNumber());
        }
        if (result.contains("${email}") && request.getEmail() != null) {
            result = result.replace("${email}", request.getEmail());
        }

        for (Map.Entry<String, Object> entry : newValues.entrySet()) {
            String placeholder1 = "${newValues." + entry.getKey() + "}";
            String placeholder2 = "${fieldValues." + entry.getKey() + "}";
            String placeholder3 = "${" + entry.getKey() + "}";
            String valStr = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder1, valStr);
            result = result.replace(placeholder2, valStr);
            result = result.replace(placeholder3, valStr);
        }

        return result;
    }
}
