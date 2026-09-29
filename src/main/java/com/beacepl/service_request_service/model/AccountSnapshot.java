package com.beacepl.service_request_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountSnapshot {
    private String id;
    private String investorCode;
    private String platformId;
    private Long backOfficeId;
    private String emailAddress;
    private String mobileNumber;
    private String name;
    private String gender;
    private String nid;
    private boolean nidVerified;
    private String fathersName;
    private String mothersName;
    private LocalDateTime dateOfBirth;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String country;
    private String state;
    private String zipCode;
    private BankEntity bank;
    private String accountNo;
    private String residency;
    private String boType;
    private String occupation;
    private String boNumber;
    private String nidFront;
    private String nidBack;
    private String photo;
    private String signature;
    private String chequeLeaf;
    private String tinCertificate;
    private String tinNumber;
    private Object jointAccountEntity;
    private List<NomineeEntity> nominees;
    private Object completionSection;
    private List<String> powerOfAttorneyForAccounts;
    private List<String> powerOfAttorneyByAccounts;
    private String accountStatus;
    private String internalStatus;
    private boolean enableDividendCredit;
    private boolean applyForTaxExemption;
    private String sourceOfFund;
    private String rmId;
    private boolean rmAccepted;
    private boolean rmContacted;
    private String csdId;
    private boolean csdContaced;
    private String remark;
    private double paymentAmount;
    private String transactionId;
    private String transactionMode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
