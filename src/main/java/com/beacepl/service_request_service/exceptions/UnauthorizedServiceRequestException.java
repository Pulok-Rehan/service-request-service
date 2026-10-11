package com.beacepl.service_request_service.exceptions;

/** Thrown when the submitter's role is not allowed to raise a service request (HTTP 403). */
public class UnauthorizedServiceRequestException extends ServiceRequestException {
    public UnauthorizedServiceRequestException(String message) {
        super(message);
    }
}
