package com.beacepl.service_request_service.service;

import com.beacepl.service_request_service.entity.FieldConfigEntity;
import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.enums.FieldDataType;
import com.beacepl.service_request_service.exceptions.InvalidRequestException;
import com.beacepl.service_request_service.service.impl.FieldValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FieldValidationServiceTest {

    private FieldValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new FieldValidationService();
    }

    @Test
    @DisplayName("Should pass validation for valid mobile number change request")
    void testValidMobileChange() {
        ServiceRequestConfigEntity config = ServiceRequestConfigEntity.builder()
                .serviceName("MOBILE_CHANGE")
                .active(true)
                .allowedActions(List.of("EDIT"))
                .fields(List.of(
                        FieldConfigEntity.builder()
                                .fieldName("newMobileNumber")
                                .label("New Mobile Number")
                                .dataType(FieldDataType.NUMBER)
                                .required(true)
                                .validationRegex("^01[3-9]\\d{8}$")
                                .build()
                ))
                .build();

        Map<String, Object> fields = Map.of("newMobileNumber", "01712345678");

        assertDoesNotThrow(() -> validationService.validateServiceAndFields(config, "EDIT", null, fields, null));
    }

    @Test
    @DisplayName("Should throw InvalidRequestException for invalid mobile number format")
    void testInvalidMobileFormat() {
        ServiceRequestConfigEntity config = ServiceRequestConfigEntity.builder()
                .serviceName("MOBILE_CHANGE")
                .active(true)
                .allowedActions(List.of("EDIT"))
                .fields(List.of(
                        FieldConfigEntity.builder()
                                .fieldName("newMobileNumber")
                                .label("New Mobile Number")
                                .dataType(FieldDataType.NUMBER)
                                .required(true)
                                .validationRegex("^01[3-9]\\d{8}$")
                                .build()
                ))
                .build();

        Map<String, Object> fields = Map.of("newMobileNumber", "12345");

        assertThrows(InvalidRequestException.class, () ->
                validationService.validateServiceAndFields(config, "EDIT", null, fields, null)
        );
    }

    @Test
    @DisplayName("Should throw InvalidRequestException when required field is missing")
    void testMissingRequiredField() {
        ServiceRequestConfigEntity config = ServiceRequestConfigEntity.builder()
                .serviceName("EMAIL_CHANGE")
                .active(true)
                .allowedActions(List.of("EDIT"))
                .fields(List.of(
                        FieldConfigEntity.builder()
                                .fieldName("newEmailAddress")
                                .label("New Email Address")
                                .dataType(FieldDataType.EMAIL)
                                .required(true)
                                .build()
                ))
                .build();

        Map<String, Object> fields = new HashMap<>();

        assertThrows(InvalidRequestException.class, () ->
                validationService.validateServiceAndFields(config, "EDIT", null, fields, null)
        );
    }

    @Test
    @DisplayName("Should throw InvalidRequestException when minor nominee is missing guardian details")
    void testMinorNomineeGuardianValidation() {
        ServiceRequestConfigEntity config = ServiceRequestConfigEntity.builder()
                .serviceName("NOMINEE_ADD")
                .active(true)
                .allowedActions(List.of("ADD"))
                .fields(List.of(
                        FieldConfigEntity.builder().fieldName("name").required(true).build()
                ))
                .build();

        Map<String, Object> fields = Map.of(
                "name", "Minor Child",
                "minor", true
        );

        assertThrows(InvalidRequestException.class, () ->
                validationService.validateServiceAndFields(config, "ADD", null, fields, null)
        );
    }
}
