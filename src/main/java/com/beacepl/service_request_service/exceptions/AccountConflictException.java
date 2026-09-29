package com.beacepl.service_request_service.exceptions;

public class AccountConflictException extends ServiceRequestException {
    public AccountConflictException(String message) {
        super(message);
    }
}
