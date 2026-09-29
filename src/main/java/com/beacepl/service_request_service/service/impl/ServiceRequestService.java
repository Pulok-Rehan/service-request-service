package com.beacepl.service_request_service.service.impl;

import com.beacepl.service_request_service.entity.ServiceRequestEntity;
import com.beacepl.service_request_service.model.ServiceRequestSubmitDto;
import com.beacepl.service_request_service.model.ServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private final ServiceRequestServiceImpl serviceRequestServiceImpl;

    public ServiceResponse<ServiceRequestEntity> submitServiceRequest(ServiceRequestSubmitDto dto, Map<String, MultipartFile> files) throws Exception {
        return serviceRequestServiceImpl.submitServiceRequest(dto, files);
    }

    public ServiceResponse<Page<ServiceRequestEntity>> getPreviousServiceRequests(String mobileNumber) {
        return serviceRequestServiceImpl.getUserRequests(mobileNumber, null, null, 0, 20);
    }
}
