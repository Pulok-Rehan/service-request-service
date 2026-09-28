package com.beacepl.service_request_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "Nominee")
public class NomineeEntity {
    @Id
    private String id;

    private String name;
    private String relation;
    private String nid;
    private LocalDate dateOfBirth;
    private double percentage;
    private String city;
    private String country;
    private String state;
    private String zipCode;
    private String address;
    private String mobileNumber;
    private String nomineeNidFront;
    private String nomineeNidBack;
    private boolean minor;
    private String guardianName;
    private String relationshipWithNominee;
    private String guardianNidNumber;
    private String guardianNidFront;
    private String guardianNidBack;
}
