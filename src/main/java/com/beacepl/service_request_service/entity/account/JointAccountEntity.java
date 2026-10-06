package com.beacepl.service_request_service.entity.account;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Date;

@Data
@Builder
@Document(collection = "JointAccountEntity")
public class JointAccountEntity {
    @Id
    private String id;
    private String name;
    private String mobileNumber;
    private String emailAddress;
    private String address;
    private Date jointAccountDob;
    private String jointAccountGender;
    private String jointAccountNationality;
//    private String jointAccountEmail;
//    private String jointAccountMobileNumber;
    private String jointAccountPhoto;
    private String jointAccountSignature;
    private String jointAccountNidFront;
    private String jointAccountNidBack;

}
