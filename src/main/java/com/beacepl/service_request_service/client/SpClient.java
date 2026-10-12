package com.beacepl.service_request_service.client;

import com.beacepl.service_request_service.exceptions.ServiceRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Client for stored procedure calls exposed by the SP service.
 * The SP endpoint path and response shape are configurable via application.properties.
 */
@Service
@Slf4j
public class SpClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${sp-service.base-url:http://10.20.242.239:9092}")
    private String baseUrl;

    @Value("${sp-service.mobile-app-check-path:/sp/check-mobile-app}")
    private String mobileAppCheckPath;

    @Value("${sp-service.mobile-app-check.enabled:false}")
    private boolean mobileAppCheckEnabled;

    public SpClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Calls the SP that checks whether the mobile app is already available for the client.
     *
     * @return true if the app is already available (request must NOT be submitted)
     */
    public boolean isMobileAppAvailable(String investorCode, String platformId) {
        if (!mobileAppCheckEnabled) {
            log.warn("Mobile app availability SP check is disabled by configuration; treating as NOT available");
            return false;
        }

        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + mobileAppCheckPath)
                .queryParam("investorCode", investorCode)
                .queryParam("platformId", platformId)
                .toUriString();

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            boolean available = parseAvailability(response.getBody());
            log.info("Mobile app availability SP result for investorCode={}, platformId={}: {}", investorCode, platformId, available);
            return available;
        } catch (Exception e) {
            log.error("Mobile app availability SP call failed for investorCode={}: {}", investorCode, e.getMessage(), e);
            // Fail closed: do not allow submission when availability cannot be verified
            throw new ServiceRequestException("Unable to verify mobile app availability. Please try again later.");
        }
    }

    /**
     * Accepts several common response shapes:
     * - plain boolean / number:            true | 1
     * - wrapped in ServiceResponse:        { "content": true } | { "content": { "available": true } }
     * - object with a flag:                { "available": true } | { "isAvailable": 1 } | { "exists": true }
     * - list of rows (non-empty = exists): [ {...} ]
     */
    boolean parseAvailability(String body) throws Exception {
        if (body == null || body.isBlank()) {
            return false;
        }
        JsonNode node = objectMapper.readTree(body);
        if (node.has("content")) {
            node = node.get("content");
            if (node.isTextual()) {
                String text = node.asText();
                if (text.trim().startsWith("{") || text.trim().startsWith("[")) {
                    node = objectMapper.readTree(text);
                }
            }
        }
        return toBoolean(node);
    }

    private boolean toBoolean(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return false;
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isNumber()) {
            return node.asInt() > 0;
        }
        if (node.isTextual()) {
            String t = node.asText().trim();
            return "true".equalsIgnoreCase(t) || "Y".equalsIgnoreCase(t) || "YES".equalsIgnoreCase(t)
                    || (t.matches("^\\d+$") && Integer.parseInt(t) > 0);
        }
        if (node.isArray()) {
            if (node.isEmpty()) {
                return false;
            }
            JsonNode first = node.get(0);
            return first.isObject() && hasFlag(first) ? toBoolean(flag(first)) : true;
        }
        if (node.isObject()) {
            return hasFlag(node) && toBoolean(flag(node));
        }
        return false;
    }

    private static final String[] FLAG_KEYS = {"available", "isAvailable", "appAvailable", "exists", "isExists", "status", "result"};

    private boolean hasFlag(JsonNode obj) {
        for (String key : FLAG_KEYS) {
            if (obj.has(key)) return true;
        }
        return false;
    }

    private JsonNode flag(JsonNode obj) {
        for (String key : FLAG_KEYS) {
            if (obj.has(key)) return obj.get(key);
        }
        return null;
    }
}
