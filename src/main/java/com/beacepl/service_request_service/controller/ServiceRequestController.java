package com.beacepl.service_request_service.controller;

import com.beacepl.service_request_service.ResponseMessageUtil;
import com.beacepl.service_request_service.exceptions.OtpNotFoundException;
import com.beacepl.service_request_service.exceptions.OtpNotMatchException;
import com.beacepl.service_request_service.model.*;
import com.beacepl.service_request_service.service.impl.ServiceRequestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/service-request")
@Slf4j
@RequiredArgsConstructor
public class ServiceRequestController {

//    private final ServiceRequestService serviceRequestService;
//
//    @PostMapping("/submit")
//    public ServiceResponse submitServiceRequest(@RequestBody GenericServiceRequestDto requestDto) throws JsonProcessingException {
//        log.info("Received dynamic request for service: {}", requestDto.getServiceName());
//        return serviceRequestService.submitServiceRequest(requestDto);
//    }
//
//    @GetMapping("/previous-requests")
//    public ServiceResponse getPreviousServiceRequests(@RequestParam String mobileNumber) throws JsonProcessingException {
//        log.info("Received request to fetch previous service requests for mobile: {}", mobileNumber);
//        return serviceRequestService.getPreviousServiceRequests(mobileNumber);
//    }
//
//    @GetMapping("/service-request-list")
//    public ServiceResponse getServiceRequestList() throws JsonProcessingException {
//        log.info("Received request to fetch service request list");
//        return serviceRequestService.getServiceRequestList();
//    }
//
//    @GetMapping("/service-request-list/{name}/{mobileNumber}")
//    public ServiceResponse getServiceRequestListByName(@PathVariable String name, @PathVariable String mobileNumber) throws JsonProcessingException {
//        log.info("Received request to fetch service request details for service: {} and mobile: {}", name, mobileNumber);
//        return serviceRequestService.getServiceRequestListByName(name, mobileNumber);
//    }
//
//    @GetMapping("/form-fields")
//    public ServiceResponse getServiceRequestForm(@RequestParam("name") String name, @RequestParam("mobileNumber") String mobileNumber) throws JsonProcessingException {
//        log.info("Received request for form-fields for service: {} and mobile: {}", name, mobileNumber);
//        return serviceRequestService.getServiceRequestListByName(name, mobileNumber);
//    }
//
//    // Deprecated legacy endpoints kept for backward compatibility
//    @PostMapping("/change-mobile")
//    public ServiceResponse changeMobileNumber(@RequestBody ChangeMobileRequestDto changeMobileRequestDto) {
//        return serviceRequestService.changeMobileNumber(changeMobileRequestDto);
//    }
//
//    @PostMapping("/change-email")
//    public ServiceResponse changeEmail(@RequestBody ChangeEmailRequestDto changeEmailRequestDto) {
//        log.info("Received request to change email to: {}", changeEmailRequestDto.getEmail());
//        return serviceRequestService.changeEmail(changeEmailRequestDto);
//    }
//
//    @PostMapping(value = "/change-bank-account", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ServiceResponse changeBankAccount(
//            @ModelAttribute ChangeBankRequestDto changeBankRequestDto,
//            @RequestParam("chequeLeaf") MultipartFile chequeLeaf) throws IOException {
//        log.info("Received request to change bank account to: {}", changeBankRequestDto.getBankAccountNumber());
//        return serviceRequestService.changeBankAccount(changeBankRequestDto, chequeLeaf);
//    }
//
//    @PostMapping(
//            value = "/tin",
//            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
//    )
//    public ServiceResponse submitTinChangeRequest(
//            @ModelAttribute TinChangeRequestDto requestDto) throws Exception {
//
//        return serviceRequestService.submitTinChangeRequest(requestDto);
//    }
//
//
//    @PostMapping(
//            value = "/nominee",
//            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
//    )
//    public ServiceResponse submitNomineeChangeRequest(
//            @ModelAttribute NomineeChangeRequestDto requestDto) throws Exception {
//
//        return serviceRequestService.submitNomineeChangeRequest(requestDto);
//    }



    private final ServiceRequestService serviceRequestService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServiceResponse submitServiceRequest(
            @RequestPart("requestData") String requestDataJson,
            @RequestParam Map<String, MultipartFile> allParts) throws Exception {
        try {

            ServiceRequestSubmitDto dto = objectMapper.readValue(requestDataJson, ServiceRequestSubmitDto.class);
            log.info("Received service request submission for service: {}", dto.getServiceName());

            // "allParts" (bound via @RequestParam Map<String, MultipartFile>) picks up
            // every multipart part that Spring can resolve as a MultipartFile - i.e.
            // every file part except requestData itself.
            Map<String, MultipartFile> files = new HashMap<>(allParts);
            files.remove("requestData");

            return serviceRequestService.submitServiceRequest(dto, files);

        } catch (OtpNotFoundException e) {
            ServiceRequestSubmitDto requestSubmitDto = objectMapper.readValue(requestDataJson, ServiceRequestSubmitDto.class);
            return ResponseMessageUtil.otpRequiredResponse(requestSubmitDto.getMobileNumber(), requestSubmitDto.getEmailAddress());
        } catch (OtpNotMatchException e) {
            return new ServiceResponse("OTP did not match", "428");
        }
    }



    @GetMapping("/previous-requests")
    public ServiceResponse getPreviousServiceRequests(@RequestParam String mobileNumber) {
        log.info("Received request to fetch previous service requests for mobile: {}", mobileNumber);
        return serviceRequestService.getPreviousServiceRequests(mobileNumber);
    }

    @GetMapping("/service-request-list")
    public ServiceResponse getServiceRequestList() {
        log.info("Received request to fetch service request list");
        return serviceRequestService.getServiceRequestList();
    }

    /** Field configuration for one service type (dynamic form definition) + the requesters history for it. */
    @GetMapping(value = "/form-fields" , produces = MediaType.APPLICATION_JSON_VALUE)
    public ServiceResponse getServiceRequestForm(@RequestParam("name") String name,
                                                 @RequestParam("mobileNumber") String mobileNumber) {
        log.info("Received request for form-fields for service: {} and mobile: {}", name, mobileNumber);
        return serviceRequestService.getServiceRequestListByName(name, mobileNumber);
    }

}