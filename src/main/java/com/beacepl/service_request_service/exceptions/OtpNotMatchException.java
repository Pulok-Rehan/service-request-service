package com.beacepl.service_request_service.exceptions;


public class OtpNotMatchException extends RuntimeException {
    public OtpNotMatchException(String message) {
        super(message);
    }
}
