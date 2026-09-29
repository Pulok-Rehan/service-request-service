package com.beacepl.service_request_service.controller;

import com.beacepl.service_request_service.exceptions.AccountConflictException;
import com.beacepl.service_request_service.exceptions.AccountNotFoundException;
import com.beacepl.service_request_service.exceptions.ConfigurationNotFoundException;
import com.beacepl.service_request_service.exceptions.DuplicateRequestException;
import com.beacepl.service_request_service.exceptions.InvalidRequestException;
import com.beacepl.service_request_service.exceptions.RequestNotFoundException;
import com.beacepl.service_request_service.exceptions.ServiceRequestException;
import com.beacepl.service_request_service.model.ServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountConflictException.class)
    public ResponseEntity<ServiceResponse<Object>> handleAccountConflict(AccountConflictException ex) {
        log.warn("Account conflict exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ServiceResponse.error(ex.getMessage(), "409"));
    }

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<ServiceResponse<Object>> handleDuplicateRequest(DuplicateRequestException ex) {
        log.warn("Duplicate request exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ServiceResponse.error(ex.getMessage(), "409"));
    }

    @ExceptionHandler(RequestNotFoundException.class)
    public ResponseEntity<ServiceResponse<Object>> handleRequestNotFound(RequestNotFoundException ex) {
        log.warn("Request not found exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ServiceResponse.error(ex.getMessage(), "404"));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ServiceResponse<Object>> handleAccountNotFound(AccountNotFoundException ex) {
        log.warn("Account not found exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ServiceResponse.error(ex.getMessage(), "404"));
    }

    @ExceptionHandler(ConfigurationNotFoundException.class)
    public ResponseEntity<ServiceResponse<Object>> handleConfigurationNotFound(ConfigurationNotFoundException ex) {
        log.warn("Configuration not found exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ServiceResponse.error(ex.getMessage(), "404"));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ServiceResponse<Object>> handleInvalidRequest(InvalidRequestException ex) {
        log.warn("Invalid request exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ServiceResponse.error(ex.getMessage(), "400"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ServiceResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        log.warn("Validation error: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ServiceResponse<>(true, "Validation failed: " + errors, errors, "400"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ServiceResponse<Object>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("File size limit exceeded: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ServiceResponse.error("Uploaded file size exceeds maximum limit (10MB)", "413"));
    }

    @ExceptionHandler(ServiceRequestException.class)
    public ResponseEntity<ServiceResponse<Object>> handleBaseException(ServiceRequestException ex) {
        log.error("Service request error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ServiceResponse.error(ex.getMessage(), "500"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ServiceResponse<Object>> handleUncaughtException(Exception ex) {
        log.error("Uncaught server exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ServiceResponse.error("Internal Server Error: " + ex.getMessage(), "500"));
    }
}
