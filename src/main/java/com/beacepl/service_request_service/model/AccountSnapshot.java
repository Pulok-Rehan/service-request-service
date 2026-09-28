package com.beacepl.service_request_service.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AccountSnapshot {
    private String investorCode;
    private String emailAddress;
    private String mobileNumber;
    private String name;
    private String gender;
    private String nid;
    private String fathersName;
    private String mothersName;
    private String dateOfBirth;
    private String addressLine1;
    private String addressLine2;
    private String tinNumber;
    private String tinCertificate;
    private String city;
    private String country;
    private String state;
    private String zipCode;
    private BankEntity bank;
    private String accountNo;
    private String residency;
    private String boType;
    private String boNumber;
    private String nidFront;
    private String nidBack;
    private String photo;
    private String signature;
    private String chequeLeaf;
    private List<NomineeEntity> nominees;
    private String rmId;
    private boolean rmAccepted;
    private boolean rmContacted;
    private String csdId;
    private boolean csdContaced;
    private String remark;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
