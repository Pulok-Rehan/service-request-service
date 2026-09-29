package com.beacepl.service_request_service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceResponse<T> {
    private boolean hasError;
    private String message;
    private T content;
    private String statusCode;

    public ServiceResponse(boolean hasError, String message, T content) {
        this.hasError = hasError;
        this.message = message;
        this.content = content;
        this.statusCode = hasError ? "400" : "200";
    }

    public ServiceResponse(String message, String statusCode) {
        this.hasError = true;
        this.message = message;
        this.content = null;
        this.statusCode = statusCode;
    }

    public ServiceResponse(String message, T content, String statusCode) {
        this.hasError = false;
        this.message = message;
        this.content = content;
        this.statusCode = statusCode;
    }

    public static <T> ServiceResponse<T> success(String message, T content) {
        return new ServiceResponse<>(false, message, content, "200");
    }

    public static <T> ServiceResponse<T> success(T content) {
        return new ServiceResponse<>(false, "Success", content, "200");
    }

    public static <T> ServiceResponse<T> error(String message) {
        return new ServiceResponse<>(true, message, null, "400");
    }

    public static <T> ServiceResponse<T> error(String message, String statusCode) {
        return new ServiceResponse<>(true, message, null, statusCode);
    }
}
