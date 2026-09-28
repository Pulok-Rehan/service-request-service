package com.beacepl.service_request_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeMobileRequestDto {
    private String mobileNumber;
    private String otp;
}
