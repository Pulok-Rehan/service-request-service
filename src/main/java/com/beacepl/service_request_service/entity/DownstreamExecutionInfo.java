package com.beacepl.service_request_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DownstreamExecutionInfo {

    private String targetUrl;

    private String httpMethod;

    private Integer httpStatus;

    private LocalDateTime executedAt;

    private String requestPayload;

    private String responseBody;

    private String errorMessage;

    /** Status: SUCCESS, FAILED */
    private String status;

    private int retryCount;
}
