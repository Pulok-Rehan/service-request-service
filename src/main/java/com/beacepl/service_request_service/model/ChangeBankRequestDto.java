package com.beacepl.service_request_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeBankRequestDto {
    private String accountId;
    private String investorCode;
    private String mobileNumber;
    private String email;
    private String otp;
    private String platformId;

    private String bankAccountNumber;
    private String bankName;
    private String branchName;
    private String routingNumber;
    private MultipartFile chequeLeaf;
}
