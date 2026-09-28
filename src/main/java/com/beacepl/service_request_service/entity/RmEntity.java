package com.beacepl.service_request_service.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Data
public class RmEntity {
    private String employeeCode;
    private String employeeName;
    private String dateOfBirth;
    private String gender;
    private String mobileNumber;
    private String emailAddress;
    private String status;
    private String cardNumber;
    private String fathersName;
    private String mothersName;
    private String branchName;
    private String branchCode;
    private String username;
    private String managersName;
    private String managerCode;
    private String managerCardNumber;
}
