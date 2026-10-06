package com.beacepl.service_request_service.controller;

import com.beacepl.service_request_service.ResponseMessageUtil;
import com.beacepl.service_request_service.entity.ServiceRequestEntity;
import com.beacepl.service_request_service.enums.ServiceRequestStatus;
import com.beacepl.service_request_service.exceptions.OtpNotFoundException;
import com.beacepl.service_request_service.exceptions.OtpNotMatchException;
import com.beacepl.service_request_service.model.*;
import com.beacepl.service_request_service.service.impl.ServiceRequestServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
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
     * Frontend API to get service configuration along with current old values from AccountEntity for a specific service.
     * GET /service-request/form/{serviceName} or GET /service-request/initial-data
     */
    @GetMapping({"/form/{serviceName}", "/initial-data", "/config-values"})
    public ServiceResponse<ServiceConfigAndOldValuesResponseDto> getFormData(
            @PathVariable(required = false) String serviceName,
            @RequestParam(required = false) String serviceNameParam,
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String investorCode,
            @RequestParam(required = false) String listItemIdentifierValue
    ) {
        String finalServiceName = (serviceName != null && !serviceName.isBlank()) ? serviceName : serviceNameParam;
        String identifier = (accountId != null && !accountId.isBlank()) ? accountId : investorCode;
        log.info("Fetching form fields and old values for identifier: {}, serviceName: {}", identifier, finalServiceName);
        return serviceRequestService.getServiceConfigWithOldValues(identifier, finalServiceName, listItemIdentifierValue);
    }

    /**
     * 1. Dedicated API: Mobile Change Request
     * POST /service-request/mobile-change
     */
    @PostMapping(value = "/mobile-change", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ServiceResponse submitMobileChange(@RequestBody ChangeMobileRequestDto dto) throws Exception {
        Map<String, Object> fields = new HashMap<>();
        fields.put("newMobileNumber", dto.getNewMobileNumber());

        ServiceRequestSubmitDto submitDto = ServiceRequestSubmitDto.builder()
                .serviceName("MOBILE_CHANGE")
                .accountId(dto.getAccountId())
                .investorCode(dto.getInvestorCode())
                .mobileNumber(dto.getMobileNumber())
                .email(dto.getEmail())
                .otp(dto.getOtp())
                .platformId(dto.getPlatformId())
                .action("EDIT")
                .fields(fields)
                .build();

        try {
            return serviceRequestService.submitServiceRequest(submitDto, null);
        } catch (OtpNotFoundException e) {
            return ResponseMessageUtil.otpRequiredResponse(dto.getMobileNumber(), dto.getEmail());
        } catch (OtpNotMatchException e) {
            return new ServiceResponse("OTP did not match", "428");
        }
    }

    /**
     * 2. Dedicated API: Email Change Request
     * POST /service-request/email-change
     */
    @PostMapping(value = "/email-change", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ServiceResponse submitEmailChange(@RequestBody ChangeEmailRequestDto dto) throws Exception {
        Map<String, Object> fields = new HashMap<>();
        fields.put("newEmailAddress", dto.getNewEmailAddress());

        ServiceRequestSubmitDto submitDto = ServiceRequestSubmitDto.builder()
                .serviceName("EMAIL_CHANGE")
                .accountId(dto.getAccountId())
                .investorCode(dto.getInvestorCode())
                .mobileNumber(dto.getMobileNumber())
                .email(dto.getEmail())
                .otp(dto.getOtp())
                .platformId(dto.getPlatformId())
                .action("EDIT")
                .fields(fields)
                .build();

        try {
            return serviceRequestService.submitServiceRequest(submitDto, null);
        } catch (OtpNotFoundException e) {
            return ResponseMessageUtil.otpRequiredResponse(dto.getMobileNumber(), dto.getEmail());
        } catch (OtpNotMatchException e) {
            return new ServiceResponse("OTP did not match", "428");
        }
    }

    /**
     * 3. Dedicated API: Address Change Request
     * POST /service-request/address-change
     */
    @PostMapping(value = "/address-change", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ServiceResponse submitAddressChange(@RequestBody ChangeAddressRequestDto dto) throws Exception {
        Map<String, Object> fields = new HashMap<>();
        if (dto.getAddressLine1() != null) fields.put("addressLine1", dto.getAddressLine1());
        if (dto.getAddressLine2() != null) fields.put("addressLine2", dto.getAddressLine2());
        if (dto.getCity() != null) fields.put("city", dto.getCity());
        if (dto.getState() != null) fields.put("state", dto.getState());
        if (dto.getZipCode() != null) fields.put("zipCode", dto.getZipCode());
        if (dto.getCountry() != null) fields.put("country", dto.getCountry());

        ServiceRequestSubmitDto submitDto = ServiceRequestSubmitDto.builder()
                .serviceName("ADDRESS_CHANGE")
                .accountId(dto.getAccountId())
                .investorCode(dto.getInvestorCode())
                .mobileNumber(dto.getMobileNumber())
                .email(dto.getEmail())
                .otp(dto.getOtp())
                .platformId(dto.getPlatformId())
                .action("EDIT")
                .fields(fields)
                .build();

        try {
            return serviceRequestService.submitServiceRequest(submitDto, null);
        } catch (OtpNotFoundException e) {
            return ResponseMessageUtil.otpRequiredResponse(dto.getMobileNumber(), dto.getEmail());
        } catch (OtpNotMatchException e) {
            return new ServiceResponse("OTP did not match", "428");
        }
    }

    /**
     * 4. Dedicated API: TIN Change Request
     * POST /service-request/tin-change
     */
    @PostMapping(value = "/tin-change", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServiceResponse submitTinChange(@ModelAttribute TinChangeRequestDto dto) throws Exception {
        Map<String, Object> fields = new HashMap<>();
        fields.put("tinNumber", dto.getTinNumber());

        Map<String, MultipartFile> files = new HashMap<>();
        if (dto.getTinCertificate() != null && !dto.getTinCertificate().isEmpty()) {
            files.put("tinCertificate", dto.getTinCertificate());
        }

        ServiceRequestSubmitDto submitDto = ServiceRequestSubmitDto.builder()
                .serviceName("TIN_CHANGE")
                .accountId(dto.getAccountId())
                .mobileNumber(dto.getMobileNumber())
                .email(dto.getEmail())
                .otp(dto.getOtp())
                .action(dto.getAction() != null ? dto.getAction() : "EDIT")
                .fields(fields)
                .build();

        try {
            return serviceRequestService.submitServiceRequest(submitDto, files);
        } catch (OtpNotFoundException e) {
            return ResponseMessageUtil.otpRequiredResponse(dto.getMobileNumber(), dto.getEmail());
        } catch (OtpNotMatchException e) {
            return new ServiceResponse("OTP did not match", "428");
        }
    }

    /**
     * 5. Dedicated API: Bank Change Request
     * POST /service-request/bank-change
     */
    @PostMapping(value = "/bank-change")
    public ServiceResponse submitBankChange(@ModelAttribute ChangeBankRequestDto dto) throws Exception {
        Map<String, Object> fields = new HashMap<>();
        fields.put("bankAccountNumber", dto.getBankAccountNumber());
        fields.put("bankName", dto.getBankName());
        fields.put("branchName", dto.getBranchName());
        fields.put("routingNumber", dto.getRoutingNumber());

        Map<String, MultipartFile> files = new HashMap<>();
        if (dto.getChequeLeaf() != null && !dto.getChequeLeaf().isEmpty()) {
            files.put("chequeLeaf", dto.getChequeLeaf());
        }

        ServiceRequestSubmitDto submitDto = ServiceRequestSubmitDto.builder()
                .serviceName("BANK_CHANGE")
                .accountId(dto.getAccountId())
                .investorCode(dto.getInvestorCode())
                .mobileNumber(dto.getMobileNumber())
                .email(dto.getEmail())
                .otp(dto.getOtp())
                .platformId(dto.getPlatformId())
                .action("EDIT")
                .fields(fields)
                .build();

        try {
            return serviceRequestService.submitServiceRequest(submitDto, files);
        } catch (OtpNotFoundException e) {
            return ResponseMessageUtil.otpRequiredResponse(dto.getMobileNumber(), dto.getEmail());
        } catch (OtpNotMatchException e) {
            return new ServiceResponse("OTP did not match", "428");
        }
    }

    /**
     * 6. Dedicated API: Nominee Add Request
     * POST /service-request/nominee/add
     */
    @PostMapping(value = "/nominee/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServiceResponse submitNomineeAdd(@ModelAttribute NomineeChangeRequestDto dto) throws Exception {
        return handleNomineeRequest(dto, "ADD", null);
    }

    /**
     * 7. Dedicated API: Nominee Edit Request
     * PUT /service-request/nominee/edit/{nomineeId}
     */
    @PutMapping(value = "/nominee/edit/{nomineeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServiceResponse submitNomineeEdit(@PathVariable String nomineeId, @ModelAttribute NomineeChangeRequestDto dto) throws Exception {
        return handleNomineeRequest(dto, "EDIT", nomineeId);
    }

    /**
     * 8. Dedicated API: Nominee Remove Request
     * DELETE /service-request/nominee/remove/{nomineeId}
     */
    @DeleteMapping(value = "/nominee/remove/{nomineeId}")
    public ServiceResponse submitNomineeRemove(
            @PathVariable String nomineeId,
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String investorCode,
            @RequestParam(required = false) String mobileNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String otp
    ) throws Exception {
        ServiceRequestSubmitDto submitDto = ServiceRequestSubmitDto.builder()
                .serviceName("NOMINEE_CHANGE")
                .accountId(accountId)
                .investorCode(investorCode)
                .mobileNumber(mobileNumber)
                .email(email)
                .otp(otp)
                .action("REMOVE")
                .listItemIdentifierValue(nomineeId)
                .fields(new HashMap<>())
                .build();

        try {
            return serviceRequestService.submitServiceRequest(submitDto, null);
        } catch (OtpNotFoundException e) {
            return ResponseMessageUtil.otpRequiredResponse(mobileNumber, email);
        } catch (OtpNotMatchException e) {
            return new ServiceResponse("OTP did not match", "428");
        }
    }

    private ServiceResponse handleNomineeRequest(NomineeChangeRequestDto dto, String action, String nomineeId) throws Exception {
        Map<String, Object> fields = new HashMap<>();
        if (dto.getName() != null) fields.put("name", dto.getName());
        if (dto.getRelation() != null) fields.put("relation", dto.getRelation());
        if (dto.getNid() != null) fields.put("nid", dto.getNid());
        if (dto.getNomineeDob() != null) fields.put("nomineeDob", dto.getNomineeDob());
        if (dto.getPercentage() != null) fields.put("percentage", dto.getPercentage());
        if (dto.getCity() != null) fields.put("city", dto.getCity());
        if (dto.getCountry() != null) fields.put("country", dto.getCountry());
        if (dto.getState() != null) fields.put("state", dto.getState());
        if (dto.getZipCode() != null) fields.put("zipCode", dto.getZipCode());
        if (dto.getAddress() != null) fields.put("address", dto.getAddress());
        if (dto.getNomineeMobileNumber() != null) fields.put("nomineeMobileNumber", dto.getNomineeMobileNumber());
        if (dto.getResidency() != null) fields.put("residency", dto.getResidency());
        if (dto.getMinor() != null) fields.put("minor", dto.getMinor());
        if (dto.getGuardianName() != null) fields.put("guardianName", dto.getGuardianName());
        if (dto.getRelationshipWithNominee() != null) fields.put("relationshipWithNominee", dto.getRelationshipWithNominee());
        if (dto.getGuardianNidNumber() != null) fields.put("guardianNidNumber", dto.getGuardianNidNumber());

        Map<String, MultipartFile> files = new HashMap<>();
        if (dto.getNomineeNidFront() != null && !dto.getNomineeNidFront().isEmpty()) files.put("nomineeNidFront", dto.getNomineeNidFront());
        if (dto.getNomineeNidBack() != null && !dto.getNomineeNidBack().isEmpty()) files.put("nomineeNidBack", dto.getNomineeNidBack());
        if (dto.getNomineePhoto() != null && !dto.getNomineePhoto().isEmpty()) files.put("nomineePhoto", dto.getNomineePhoto());
        if (dto.getNomineeSignature() != null && !dto.getNomineeSignature().isEmpty()) files.put("nomineeSignature", dto.getNomineeSignature());
        if (dto.getGuardianNidFront() != null && !dto.getGuardianNidFront().isEmpty()) files.put("guardianNidFront", dto.getGuardianNidFront());
        if (dto.getGuardianNidBack() != null && !dto.getGuardianNidBack().isEmpty()) files.put("guardianNidBack", dto.getGuardianNidBack());
        if (dto.getGuardianSignature() != null && !dto.getGuardianSignature().isEmpty()) files.put("guardianSignature", dto.getGuardianSignature());

        String serviceName = "ADD".equalsIgnoreCase(action) ? "NOMINEE_ADD" : "NOMINEE_EDIT";

        ServiceRequestSubmitDto submitDto = ServiceRequestSubmitDto.builder()
                .serviceName(serviceName)
                .accountId(dto.getAccountId())
                .mobileNumber(dto.getMobileNumber())
                .email(dto.getEmail())
                .otp(dto.getOtp())
                .action(action)
                .listItemIdentifierValue(nomineeId != null ? nomineeId : dto.getNid())
                .fields(fields)
                .build();

        try {
            return serviceRequestService.submitServiceRequest(submitDto, files);
        } catch (OtpNotFoundException e) {
            return ResponseMessageUtil.otpRequiredResponse(dto.getMobileNumber(), dto.getEmail());
        } catch (OtpNotMatchException e) {
            return new ServiceResponse("OTP did not match", "428");
        }
    }

    /**
     * Legacy generic submission endpoint for JSON payload (retained for backward compatibility).
     * POST /service-request/submit
     */
    @PostMapping(value = "/submit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ServiceResponse submitJson(@RequestBody ServiceRequestSubmitDto requestDto) throws Exception {
        try {
            log.info("Received JSON service request submission for service: {}", requestDto.getServiceName());
            return serviceRequestService.submitServiceRequest(requestDto, null);
        } catch (OtpNotFoundException e) {
            return ResponseMessageUtil.otpRequiredResponse(requestDto.getMobileNumber(), requestDto.getEmailAddress());
        } catch (OtpNotMatchException e) {
            return new ServiceResponse("OTP did not match", "428");
        }
    }

    /**
     * Legacy generic submission endpoint for Multipart payload (retained for backward compatibility).
     * POST /service-request/submit
     */
    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServiceResponse submitMultipart(
            @RequestPart(value = "requestData", required = false) String requestDataJson,
            MultipartHttpServletRequest multipartRequest
    ) throws Exception {
        ServiceRequestSubmitDto dto;

        if (requestDataJson != null && !requestDataJson.isBlank()) {
            dto = objectMapper.readValue(requestDataJson, ServiceRequestSubmitDto.class);
        } else {
            dto = ServiceRequestSubmitDto.builder()
                    .serviceName(multipartRequest.getParameter("serviceName"))
                    .accountId(multipartRequest.getParameter("accountId"))
                    .mobileNumber(multipartRequest.getParameter("mobileNumber"))
                    .investorCode(multipartRequest.getParameter("investorCode"))
                    .email(multipartRequest.getParameter("email"))
                    .otp(multipartRequest.getParameter("otp"))
                    .action(multipartRequest.getParameter("action"))
                    .platformId(multipartRequest.getParameter("platformId"))
                    .listItemIdentifierValue(multipartRequest.getParameter("listItemIdentifierValue"))
                    .fields(extractFieldsFromParams(multipartRequest.getParameterMap()))
                    .build();
        }

        Map<String, MultipartFile> uploadedFiles = new HashMap<>(multipartRequest.getFileMap());
        uploadedFiles.remove("requestData");

        try {
            return serviceRequestService.submitServiceRequest(dto, uploadedFiles);
        } catch (OtpNotFoundException e) {
            ServiceRequestSubmitDto requestSubmitDto = (requestDataJson != null && !requestDataJson.isBlank())
                    ? objectMapper.readValue(requestDataJson, ServiceRequestSubmitDto.class)
                    : dto;
            return ResponseMessageUtil.otpRequiredResponse(requestSubmitDto.getMobileNumber(), requestSubmitDto.getEmailAddress());
        } catch (OtpNotMatchException e) {
            return new ServiceResponse("OTP did not match", "428");
        }
    }

    /**
     * User request history endpoint.
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
     * Fetch request by ID.
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
                "otp".equalsIgnoreCase(paramName) ||
                "action".equalsIgnoreCase(paramName) ||
                "listItemIdentifierValue".equalsIgnoreCase(paramName);
    }
}