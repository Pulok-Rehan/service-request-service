package com.beacepl.service_request_service.entity.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "partialAccountDetails")
public class PartialAccountEntity {
    @Id
    private String id;
    private String investorCode;
    private String emailAddress;
    private String mobileNumber;
    private String passportNumber;
    private String name;
    private String gender;
    private String nid;
    private String fathersName;
    private String mothersName;
    private LocalDateTime dateOfBirth;
    private String platformId;
    private String addressLine1PresentAddress;
    private String cityPresentAddress;
    private String countryPresentAddress;
    private String statePresentAddress;
    private String zipCodePresentAddress;
    private String addressLine1PermanentAddress;
    private String cityPermanentAddress;
    private String countryPermanentAddress;
    private String statePermanentAddress;
    private String zipCodePernmanentAddress;
    private BankEntity bank;
    private String accountNo;
    private String tinNumber;
    private boolean enableDividendCredit;
    private boolean applyForTaxExemption;
    private String residency;
    private String boType;
    private String nidFront;
    private String nidBack;
    private String photo;
    private String signature;
    private String chequeLeaf;
    private String tinCertificate;
    private boolean boLinked;
    private JointAccountEntity jointAccountEntity;
    @DBRef
    private List<NomineeEntity> nominees;
//    private CompletionSection completionSection;
    private boolean isActive;
    private String accountStatus;
    private InternalStatus internalStatus;
    private String transactionStatus;
    private String occupation;
    private String sourceOfFund;
    private String csdId;
    private String rmId;
    private String rmName;
    private String preferedBranch;
    private boolean csdContacted;
    private String remark;
    private String salary;
    private String boNumber;
    private double paymentAmount;
    private String transactionId;
    private String transactionMode;
}
