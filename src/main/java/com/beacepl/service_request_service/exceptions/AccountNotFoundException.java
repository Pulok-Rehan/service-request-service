package com.beacepl.service_request_service.exceptions;

public class AccountNotFoundException extends ServiceRequestException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
