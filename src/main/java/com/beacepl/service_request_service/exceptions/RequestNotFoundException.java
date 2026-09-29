package com.beacepl.service_request_service.exceptions;

public class RequestNotFoundException extends ServiceRequestException {
    public RequestNotFoundException(String message) {
        super(message);
    }
}
