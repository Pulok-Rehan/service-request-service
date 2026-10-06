package com.beacepl.service_request_service.entity.account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

//@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "AccountDetails")
public class AccountEntity {
    @Id
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
    private JointAccountEntity jointAccountEntity;
    @DBRef
    private List<NomineeEntity> nominees;
    private CompletionSectionEntity completionSection;
    private List<String> powerOfAttorneyForAccounts;
    private List<String> powerOfAttorneyByAccounts;
    private AccountStatus accountStatus;
    private InternalStatus internalStatus;
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
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

}
