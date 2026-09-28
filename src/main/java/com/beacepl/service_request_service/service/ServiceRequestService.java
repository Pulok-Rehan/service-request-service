package com.beacepl.service_request_service.service;

import com.beacepl.service_request_service.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ServiceRequestService {
    ServiceResponse submitServiceRequest(GenericServiceRequestDto requestDto) throws JsonProcessingException;

    ServiceResponse getPreviousServiceRequests(String mobileNumber) throws JsonProcessingException;

    ServiceResponse getServiceRequestList() throws JsonProcessingException;

    ServiceResponse getServiceRequestListByName(String name, String mobileNumber) throws JsonProcessingException;

    ServiceResponse approveRequest(String requestId);

    // Legacy/compatibility endpoints
    ServiceResponse changeMobileNumber(ChangeMobileRequestDto changeMobileRequestDto);

    ServiceResponse changeEmail(ChangeEmailRequestDto changeEmailRequestDto);

    ServiceResponse changeBankAccount(ChangeBankRequestDto changeBankRequestDto, MultipartFile chequeLeaf) throws IOException;
    ServiceResponse submitTinChangeRequest(
            TinChangeRequestDto dto) throws Exception;
    ServiceResponse submitNomineeChangeRequest(
            NomineeChangeRequestDto dto) throws Exception;
}
