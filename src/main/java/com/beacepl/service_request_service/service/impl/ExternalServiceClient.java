package com.beacepl.service_request_service.service.impl;

import com.beacepl.service_request_service.entity.RmEntity;
import com.beacepl.service_request_service.model.ServiceResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ExternalServiceClient {
    @Value("${external-service.baseUrl}")
    private String baseUrl;
    @Value("${external-service.searchRm}")
    private String searchRm;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ExternalServiceClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public RmEntity findRm(String employeeCode) throws JsonProcessingException {

        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + searchRm)
                .queryParam("accountId", employeeCode)
                .toUriString();

        ResponseEntity<ServiceResponse> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        ServiceResponse.class
                );

        ServiceResponse serviceResponse = response.getBody();

        if (serviceResponse == null || serviceResponse.isHasError()) {
            return null;
        }

        String content = String.valueOf(serviceResponse.getContent());

        return objectMapper.readValue(
                content,
                RmEntity.class
        );
    }
}
