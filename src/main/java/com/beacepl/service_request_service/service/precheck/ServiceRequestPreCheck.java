package com.beacepl.service_request_service.service.precheck;

import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.model.ServiceRequestSubmitDto;

/**
 * Service-specific validation executed before a request is submitted (before OTP is sent).
 * Throw an exception to block the submission.
 */
public interface ServiceRequestPreCheck {

    boolean supports(String serviceName);

    void check(ServiceRequestSubmitDto submitDto, ServiceRequestConfigEntity config);
}
