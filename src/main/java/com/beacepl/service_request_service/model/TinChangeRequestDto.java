package com.beacepl.service_request_service.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class TinChangeRequestDto {

    private String accountId;
    private String email;
    private String mobileNumber;

    private String otp;

    private String action; // ADD / UPDATE

    private String tinNumber;

    private MultipartFile tinCertificate;
}