package com.beacepl.service_request_service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Commission Change Request - submitted by RM only.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommissionChangeRequestDto {

    @NotBlank(message = "investorCode is required")
    private String investorCode;

    @NotBlank(message = "platformId is required")
    private String platformId;

    @NotNull(message = "ledgerBalance is required")
    private BigDecimal ledgerBalance;

    @NotNull(message = "totalEquity is required")
    private BigDecimal totalEquity;

    @NotNull(message = "netDeposit is required")
    private BigDecimal netDeposit;

    @NotNull(message = "existingCommissionRate is required")
    @DecimalMin(value = "0.0", message = "existingCommissionRate cannot be negative")
    private BigDecimal existingCommissionRate;

    @NotBlank(message = "accountType is required")
    private String accountType;

    @NotNull(message = "proposedCommissionRate is required")
    @DecimalMin(value = "0.0", message = "proposedCommissionRate cannot be negative")
    private BigDecimal proposedCommissionRate;

    private String remarks;

    /** RM employee code / user id of the submitter */
    private String requestedBy;

    /** Fallback when X-User-Role header is not present */
    private String requestedByRole;

    private String accountId;
    private String mobileNumber;
    private String email;
    private String otp;
}
