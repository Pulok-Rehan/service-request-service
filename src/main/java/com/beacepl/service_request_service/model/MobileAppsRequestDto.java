package com.beacepl.service_request_service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mobile Apps Request - submitted by RM or Client.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileAppsRequestDto {

    @NotBlank(message = "investorCode is required")
    private String investorCode;

    @NotBlank(message = "platformId is required")
    private String platformId;

    @NotNull(message = "saleableStock is required")
    private Boolean saleableStock;

    @NotNull(message = "availableBalance is required")
    private Boolean availableBalance;

    private String requestedBy;

    /** Fallback when X-User-Role header is not present */
    private String requestedByRole;

    private String accountId;
    private String mobileNumber;
    private String email;
    private String otp;
}
