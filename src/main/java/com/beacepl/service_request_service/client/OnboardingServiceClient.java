package com.beacepl.service_request_service.client;

import com.beacepl.service_request_service.exceptions.AccountNotFoundException;
import com.beacepl.service_request_service.exceptions.ServiceRequestException;
import com.beacepl.service_request_service.model.AccountSnapshot;
import com.beacepl.service_request_service.model.ApplyServiceRequestDto;
import com.beacepl.service_request_service.model.ServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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

    @Value("${onboarding.service.url:http://localhost:9092}")
    private String baseUrl;

    public AccountSnapshot getAccountById(String accountId) {
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path("/onboarding/account/")
                .path(accountId)
                .toUriString();

        try {
            log.info("Fetching account info from Onboarding Service by ID: {}", accountId);
            ResponseEntity<ServiceResponse<AccountSnapshot>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ServiceResponse<AccountSnapshot>>() {}
            );

            if (response.getBody() != null && !response.getBody().isHasError() && response.getBody().getContent() != null) {
                return response.getBody().getContent();
            }
            // Fallback to direct object if Onboarding Service returns raw AccountSnapshot
            return fetchDirectAccount(url);

        } catch (HttpClientErrorException.NotFound e) {
            throw new AccountNotFoundException("Account not found in Onboarding Service with ID: " + accountId);
        } catch (Exception e) {
            log.warn("Failed to fetch account via /onboarding/account/{}, trying search fallback. Error: {}", accountId, e.getMessage());
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
            ResponseEntity<AccountSnapshot> response = restTemplate.getForEntity(url, AccountSnapshot.class);
            if (response.getBody() != null) {
                return response.getBody();
            }
            throw new AccountNotFoundException("Account not found with identifier: " + identifier);
        } catch (HttpClientErrorException.NotFound e) {
            throw new AccountNotFoundException("Account not found with identifier: " + identifier);
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

    private AccountSnapshot fetchDirectAccount(String url) {
        ResponseEntity<AccountSnapshot> response = restTemplate.getForEntity(url, AccountSnapshot.class);
        if (response.getBody() != null) {
            return response.getBody();
        }
        throw new AccountNotFoundException("Account details not returned from Onboarding Service");
    }
}
