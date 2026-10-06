package com.beacepl.service_request_service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceResponse<T> {

    private boolean hasError;
    private String message;
    private T content;
    private String statusCode;

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public ServiceResponse(boolean hasError, String message, T content, String statusCode) {
        this.hasError = hasError;
        this.message = message;
        this.content = hasError ? content : serializeContent(content);
        this.statusCode = statusCode;
    }

    public ServiceResponse(String message, String statusCode) {
        this(true, message, null, statusCode);
    }

    public ServiceResponse(String message, T content, String statusCode) {
        this(false, message, content, statusCode);
    }

    public ServiceResponse(boolean hasError, String message, T content) {
        this(hasError, message, content, hasError ? "400" : "200");
    }

    public static <T> ServiceResponse<T> success(String message, T content) {
        return new ServiceResponse<>(false, message, content, "200");
    }

    public static <T> ServiceResponse<T> success(T content) {
        return new ServiceResponse<>(false, "Data fetched successfully", content, "200");
    }

    public static <T> ServiceResponse<T> error(String message) {
        return new ServiceResponse<>(true, message, null, "400");
    }

    public static <T> ServiceResponse<T> error(String message, String statusCode) {
        return new ServiceResponse<>(true, message, null, statusCode);
    }

    @SuppressWarnings("unchecked")
    private static <T> T serializeContent(Object content) {
        if (content == null) {
            return null;
        }
        if (content instanceof String) {
            return (T) content;
        }
        try {
            return (T) MAPPER.writeValueAsString(content);
        } catch (Exception e) {
            return (T) content.toString();
        }
    }
}
