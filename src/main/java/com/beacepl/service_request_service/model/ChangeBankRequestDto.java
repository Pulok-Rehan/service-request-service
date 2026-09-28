package com.beacepl.service_request_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeBankRequestDto {
    private String bankAccountNumber;
    private String bankName;
    private String branchName;
    private String routingNumber;
    private String otp;
}
