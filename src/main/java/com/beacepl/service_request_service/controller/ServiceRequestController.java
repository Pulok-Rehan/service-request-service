package com.beacepl.service_request_service.controller;

import com.beacepl.service_request_service.entity.ServiceRequestEntity;
import com.beacepl.service_request_service.enums.ServiceRequestStatus;
import com.beacepl.service_request_service.model.ServiceRequestSubmitDto;
import com.beacepl.service_request_service.model.ServiceResponse;
import com.beacepl.service_request_service.service.impl.ServiceRequestServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/service-request")
@Slf4j
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestServiceImpl serviceRequestService;
    private final ObjectMapper objectMapper;

    /**
     * Generic User Submission API accepting application/json.
     * POST /service-request/submit
     */
    @PostMapping(value = "/submit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ServiceResponse<ServiceRequestEntity> submitJson(
            @RequestBody ServiceRequestSubmitDto requestDto
    ) throws Exception {
        log.info("Received JSON service request submission for service: {}", requestDto.getServiceName());
        return serviceRequestService.submitServiceRequest(requestDto, null);
    }

    /**
     * Generic User Submission API accepting multipart/form-data.
     * POST /service-request/submit
     */
    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServiceResponse<ServiceRequestEntity> submitMultipart(
            @RequestPart(value = "requestData", required = false) String requestDataJson,
            MultipartHttpServletRequest multipartRequest
    ) throws Exception {
        ServiceRequestSubmitDto dto;

        if (requestDataJson != null && !requestDataJson.isBlank()) {
            dto = objectMapper.readValue(requestDataJson, ServiceRequestSubmitDto.class);
        } else {
            // Read parameters from multipart form fields
            dto = ServiceRequestSubmitDto.builder()
                    .serviceName(multipartRequest.getParameter("serviceName"))
                    .accountId(multipartRequest.getParameter("accountId"))
                    .mobileNumber(multipartRequest.getParameter("mobileNumber"))
                    .investorCode(multipartRequest.getParameter("investorCode"))
                    .email(multipartRequest.getParameter("email"))
                    .action(multipartRequest.getParameter("action"))
                    .listItemIdentifierValue(multipartRequest.getParameter("listItemIdentifierValue"))
                    .fields(extractFieldsFromParams(multipartRequest.getParameterMap()))
                    .build();
        }

        Map<String, MultipartFile> uploadedFiles = new HashMap<>(multipartRequest.getFileMap());
        uploadedFiles.remove("requestData");

        log.info("Received Multipart service request submission for service: {}, uploaded files count: {}",
                dto.getServiceName(), uploadedFiles.size());

        return serviceRequestService.submitServiceRequest(dto, uploadedFiles);
    }

    /**
     * Retrieve user service request history.
     * GET /service-request/my-requests
     */
    @GetMapping("/my-requests")
    public ServiceResponse<Page<ServiceRequestEntity>> getMyRequests(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String mobileNumber,
            @RequestParam(required = false) String investorCode,
            @RequestParam(required = false) ServiceRequestStatus status,
            @RequestParam(required = false) String serviceName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String identifier = accountId != null ? accountId : (investorCode != null ? investorCode : mobileNumber);
        if (identifier == null || identifier.isBlank()) {
            identifier = "";
        }
        return serviceRequestService.getUserRequests(identifier, status, serviceName, page, size);
    }

    /**
     * Fetch a specific request by ID.
     * GET /service-request/{id}
     */
    @GetMapping("/{id}")
    public ServiceResponse<ServiceRequestEntity> getRequestById(@PathVariable String id) {
        return serviceRequestService.getRequestById(id);
    }

    private Map<String, Object> extractFieldsFromParams(Map<String, String[]> paramMap) {
        Map<String, Object> fields = new HashMap<>();
        if (paramMap == null) return fields;

        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            if (isReservedParam(key)) continue;
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                fields.put(key, values[0]);
            }
        }
        return fields;
    }

    private boolean isReservedParam(String paramName) {
        return "serviceName".equalsIgnoreCase(paramName) ||
                "accountId".equalsIgnoreCase(paramName) ||
                "mobileNumber".equalsIgnoreCase(paramName) ||
                "investorCode".equalsIgnoreCase(paramName) ||
                "email".equalsIgnoreCase(paramName) ||
                "action".equalsIgnoreCase(paramName) ||
                "listItemIdentifierValue".equalsIgnoreCase(paramName);
    }
}