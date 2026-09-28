package com.beacepl.service_request_service.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class NomineeChangeRequestDto {

    private String accountId;
    private String email;
    private String mobileNumber;

    private String otp;

    private String action; // ADD / UPDATE

    private String name;
    private String relation;
    private String nid;

    private String nomineeDob;

    private Double percentage;

    private String city;
    private String country;
    private String state;
    private String zipCode;
    private String address;

    private String nomineeMobileNumber;

    private String residency;

    private Boolean minor;

    private String guardianName;
    private String relationshipWithNominee;
    private String guardianNidNumber;

    private MultipartFile nomineeNidFront;
    private MultipartFile nomineeNidBack;
    private MultipartFile nomineePhoto;
    private MultipartFile nomineeSignature;

    private MultipartFile guardianNidFront;
    private MultipartFile guardianNidBack;
    private MultipartFile guardianSignature;
}