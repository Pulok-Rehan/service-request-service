package com.beacepl.service_request_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NomineeEntity {
    private String id;
    private String name;
    private String relation;
    private String nid;
    private LocalDateTime nomineeDob;
    private LocalDateTime dateOfBirth;
    private double percentage;
    private String city;
    private String country;
    private String state;
    private String zipCode;
    private String address;
    private String mobileNumber;
    private String residency;
    private String nomineeNidFront;
    private String nomineeNidBack;
    private String nomineePhoto;
    private String nomineeSignature;
    private boolean minor;
    private String guardianName;
    private String relationshipWithNominee;
    private String guardianNidNumber;
    private String guardianNidFront;
    private String guardianNidBack;
    private String guardianSignature;
}
