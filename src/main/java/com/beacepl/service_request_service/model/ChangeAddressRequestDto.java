package com.beacepl.service_request_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeAddressRequestDto {
    private String accountId;
    private String investorCode;
    private String mobileNumber;
    private String email;
    private String otp;
    private String platformId;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
