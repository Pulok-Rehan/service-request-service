package com.beacepl.service_request_service.exceptions;

/** Thrown when the mobile app is already available for the client (HTTP 409). */
public class MobileAppAlreadyAvailableException extends ServiceRequestException {
    public MobileAppAlreadyAvailableException(String message) {
        super(message);
    }
}
