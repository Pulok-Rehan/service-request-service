package com.beacepl.service_request_service.exceptions;

public class ServiceRequestException extends RuntimeException {
    public ServiceRequestException(String message) {
        super(message);
    }

    public ServiceRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
