package com.beacepl.service_request_service.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceRequestSubmitDto {

    private String serviceName;

    private String accountId;

    private String mobileNumber;

    @JsonAlias({"email", "emailAddress"})
    private String email;

    private String investorCode;

    private String otp;

    private String action;

    @JsonAlias({"fields", "fieldValues"})
    private Map<String, Object> fields;

    private String listItemIdentifierValue;

    public String getEmailAddress() {
        return email;
    }

    public Map<String, Object> getFieldValues() {
        return fields;
    }
}
