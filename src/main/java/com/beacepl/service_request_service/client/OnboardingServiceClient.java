package com.beacepl.service_request_service.client;

import com.beacepl.service_request_service.exceptions.AccountNotFoundException;
import com.beacepl.service_request_service.exceptions.ServiceRequestException;
import com.beacepl.service_request_service.model.AccountSnapshot;
import com.beacepl.service_request_service.model.ApplyServiceRequestDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${onboarding.service.url:http://localhost:9092}")
    private String baseUrl;

    public AccountSnapshot getAccountById(String accountId) {
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path("/onboarding/api/search-information")
                .queryParam("input", accountId)
                .toUriString();

        try {
            log.info("Fetching account info from Onboarding Service by ID/input: {}", accountId);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getBody() == null || response.getBody().isBlank()) {
                throw new AccountNotFoundException("Account not found in Onboarding Service with ID: " + accountId);
            }

            return extractAccountSnapshotFromRawResponse(response.getBody(), accountId);

        } catch (HttpClientErrorException.NotFound e) {
            throw new AccountNotFoundException("Account not found in Onboarding Service with ID: " + accountId);
        } catch (AccountNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to fetch account via /onboarding/api/search-information, trying search fallback. Error: {}", e.getMessage());
            return searchAccount(accountId);
        }
    }

    public AccountSnapshot searchAccount(String identifier) {
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path("/onboarding/account/search")
                .queryParam("input", identifier)
                .queryParam("query", identifier)
                .toUriString();

        try {
            log.info("Searching account in Onboarding Service with identifier: {}", identifier);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getBody() != null && !response.getBody().isBlank()) {
                return extractAccountSnapshotFromRawResponse(response.getBody(), identifier);
            }
            throw new AccountNotFoundException("Account not found with identifier: " + identifier);
        } catch (HttpClientErrorException.NotFound e) {
            throw new AccountNotFoundException("Account not found with identifier: " + identifier);
        } catch (AccountNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error communicating with Onboarding Service during search: {}", e.getMessage(), e);
            throw new ServiceRequestException("Failed to communicate with Onboarding Service: " + e.getMessage(), e);
        }
    }

    public void applyServiceRequest(ApplyServiceRequestDto applyDto) {
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path("/onboarding/service-request/apply")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ApplyServiceRequestDto> request = new HttpEntity<>(applyDto, headers);

        try {
            log.info("Sending approved service request update to Onboarding Service: accountId={}, serviceName={}",
                    applyDto.getAccountId(), applyDto.getServiceName());
            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
            log.info("Successfully applied service request changes to Onboarding Service for accountId: {}", applyDto.getAccountId());
        } catch (Exception e) {
            log.error("Failed to apply service request to Onboarding Service for accountId: {}. Error: {}",
                    applyDto.getAccountId(), e.getMessage(), e);
            throw new ServiceRequestException("Onboarding Service failed to apply update: " + e.getMessage(), e);
        }
    }

    private AccountSnapshot extractAccountSnapshotFromRawResponse(String rawBody, String identifier) throws Exception {
        JsonNode rootNode = objectMapper.readTree(rawBody);

        // Check if response is wrapped in ServiceResponse { "hasError": false, "content": ... }
        if (rootNode.has("content") && !rootNode.get("content").isNull()) {
            JsonNode contentNode = rootNode.get("content");
            if (contentNode.isTextual()) {
                // content is a serialized JSON string e.g. "{\"id\":\"...\"}"
                return objectMapper.readValue(contentNode.asText(), AccountSnapshot.class);
            } else if (contentNode.isObject()) {
                // content is a JSON object
                return objectMapper.treeToValue(contentNode, AccountSnapshot.class);
            }
        }

        // If root is directly the AccountSnapshot JSON object
        if (rootNode.isObject()) {
            return objectMapper.treeToValue(rootNode, AccountSnapshot.class);
        }

        throw new AccountNotFoundException("Unable to parse account details for: " + identifier);
    }
}
