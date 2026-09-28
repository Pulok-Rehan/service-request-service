package com.beacepl.service_request_service.client;

import com.beacepl.service_request_service.model.AccountPatchRequestDto;
import com.beacepl.service_request_service.model.AccountSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class AccountServiceClient {

    private final RestTemplate restTemplate;

    @Value("${onboarding.service.url}")
    private String baseUrl;

    public AccountSnapshot getAccountSnapshot(String input) {

        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path("/onboarding/api/search")
                .queryParam("input", input)
                .toUriString();

        ResponseEntity<AccountSnapshot> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        AccountSnapshot.class
                );

        return response.getBody();
    }

    public void applyChanges(AccountPatchRequestDto request) {

        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path("/onboarding/settlement/apply-change")
                .toUriString();

        HttpEntity<AccountPatchRequestDto> entity =
                new HttpEntity<>(request);

        restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                entity,
                Void.class
        );
    }
}