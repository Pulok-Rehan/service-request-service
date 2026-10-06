package com.beacepl.service_request_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeMobileRequestDto {
    private String accountId;
    private String investorCode;
    private String mobileNumber;
    private String newMobileNumber;
    private String email;
    private String otp;
    private String platformId;
}
