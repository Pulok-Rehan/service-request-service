package com.beacepl.service_request_service.entity.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
//@Table(name = "completion_sections")
@Document(collection = "CompletionSection")
public class CompletionSectionEntity {
    @Id
    private String id;

    private String platformId;

//    private String mobileNumber;
//    private String emailAddress;
    private PartialAccountEntity singleAccount;
    private PartialAccountEntity jointAccount;

    private boolean singleAccountPersonalDetails;
    private boolean singleAccountLiveValidation;
    private boolean singleAccountBankDetails;
    private boolean singleAccountNomineeDetails;
    private boolean singleAccountDocuments;
    private boolean singleAccountBoPayment;
    private boolean singleAccountEkyc;
    private boolean singleAccountIsActive;

    private boolean jointAccountPersonalDetails;
    private boolean jointAccountLiveValidation;
    private boolean jointAccountBankDetails;
    private boolean jointAccountNomineeDetails;
    private boolean jointAccountDocuments;
    private boolean jointAccountBoPayment;
    private boolean jointAccountEkyc;
    private boolean jointAccountIsActive;

    private List<SectionChangeRequest> singleAccountSectionChanges;
    private List<SectionChangeRequest> jointAccountSectionChanges;

    @Transient
    private String singleAccountNextCorrectionStep;
    @Transient
    private String jointAccountNextCorrectionStep;
}
