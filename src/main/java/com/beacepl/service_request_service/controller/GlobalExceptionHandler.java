package com.beacepl.service_request_service.controller;

import com.beacepl.service_request_service.model.ServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ServiceResponse> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ServiceResponse(true, e.getMessage(), null, "400"));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ServiceResponse> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ServiceResponse(true, e.getMessage(), null, "404"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ServiceResponse> handleConflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ServiceResponse(true, e.getMessage(), null, "409"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ServiceResponse> handleGeneric(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ServiceResponse(true, "Something went wrong: " + e.getMessage(), null, "500"));
    }
}
