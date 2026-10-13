package com.beacepl.service_request_service.service;

import com.beacepl.service_request_service.entity.FieldConfigEntity;
import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.model.AccountSnapshot;
import com.beacepl.service_request_service.model.BankEntity;
import com.beacepl.service_request_service.model.NomineeEntity;
import com.beacepl.service_request_service.service.impl.OldValueResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OldValueResolverTest {

    private OldValueResolver oldValueResolver;

    @BeforeEach
    void setUp() {
        oldValueResolver = new OldValueResolver(new ObjectMapper());
    }

    @Test
    @DisplayName("Should resolve simple root account fields as old values")
    void testResolveSimpleOldValues() {
        AccountSnapshot account = AccountSnapshot.builder()
                .mobileNumber("01711111111")
                .emailAddress("old@example.com")
                .build();

        ServiceRequestConfigEntity config = ServiceRequestConfigEntity.builder()
                .serviceName("MOBILE_CHANGE")
                .fields(List.of(
                        FieldConfigEntity.builder().fieldName("newMobileNumber").accountFieldPath("mobileNumber").required(true).build()
                ))
                .build();

        Map<String, Object> oldValues = oldValueResolver.resolveOldValues(account, config, null, Map.of("newMobileNumber", "01822222222"));

        assertNotNull(oldValues);
        assertEquals("01711111111", oldValues.get("mobileNumber"));
    }

    @Test
    @DisplayName("Should resolve nested object paths like bank.accountNo")
    void testResolveNestedOldValues() {
        AccountSnapshot account = AccountSnapshot.builder()
                .bank(BankEntity.builder().accountNo("123456789").bankName("BRAC Bank").build())
                .build();

        ServiceRequestConfigEntity config = ServiceRequestConfigEntity.builder()
                .serviceName("BANK_CHANGE")
                .fields(List.of(
                        FieldConfigEntity.builder().fieldName("accountNo").accountFieldPath("bank.accountNo").required(true).build()
                ))
                .build();

        Map<String, Object> oldValues = oldValueResolver.resolveOldValues(account, config, null, Map.of("accountNo", "987654321"));

        assertNotNull(oldValues);
        assertEquals("123456789", oldValues.get("bank.accountNo"));
    }

    @Test
    @DisplayName("Should resolve flat bank fields like bankAccountNumber, bankName, branchName, routingNumber")
    void testResolveFlatBankFields() {
        AccountSnapshot account = AccountSnapshot.builder()
                .bank(BankEntity.builder()
                        .accountNo("1234567890785")
                        .bankName("Brac Bank Ltd.")
                        .branchName("AGRABAD")
                        .routingNumber("060150132")
                        .build())
                .build();

        ServiceRequestConfigEntity config = ServiceRequestConfigEntity.builder()
                .serviceName("BANK_CHANGE")
                .fields(List.of(
                        FieldConfigEntity.builder().fieldName("bankAccountNumber").accountFieldPath("bankAccountNumber").required(true).build(),
                        FieldConfigEntity.builder().fieldName("bankName").accountFieldPath("bankName").required(true).build(),
                        FieldConfigEntity.builder().fieldName("branchName").accountFieldPath("branchName").required(true).build(),
                        FieldConfigEntity.builder().fieldName("routingNumber").accountFieldPath("routingNumber").required(true).build()
                ))
                .build();

        Map<String, Object> oldValues = oldValueResolver.resolveOldValues(account, config, null, null);

        assertNotNull(oldValues);
        assertEquals("1234567890785", oldValues.get("bankAccountNumber"));
        assertEquals("Brac Bank Ltd.", oldValues.get("bankName"));
        assertEquals("AGRABAD", oldValues.get("branchName"));
        assertEquals("060150132", oldValues.get("routingNumber"));
    }

    @Test
    @DisplayName("Should resolve list item values for matching nominee NID")
    void testResolveListBasedNomineeOldValues() {
        NomineeEntity nominee1 = NomineeEntity.builder().nid("11111").name("John Doe").relation("Brother").percentage(50).build();
        NomineeEntity nominee2 = NomineeEntity.builder().nid("22222").name("Jane Doe").relation("Sister").percentage(50).build();

        AccountSnapshot account = AccountSnapshot.builder()
                .nominees(List.of(nominee1, nominee2))
                .build();

        ServiceRequestConfigEntity config = ServiceRequestConfigEntity.builder()
                .serviceName("NOMINEE_EDIT")
                .listBased(true)
                .targetListField("nominees")
                .listIdentifierField("nid")
                .fields(List.of(
                        FieldConfigEntity.builder().fieldName("name").accountFieldPath("name").build(),
                        FieldConfigEntity.builder().fieldName("relation").accountFieldPath("relation").build()
                ))
                .build();

        Map<String, Object> oldValues = oldValueResolver.resolveListBasedOldValues(account, config, "22222");

        assertNotNull(oldValues);
        assertEquals("Jane Doe", oldValues.get("name"));
        assertEquals("Sister", oldValues.get("relation"));
    }

    @Test
    @DisplayName("Should resolve optional field as old value when account has existing value, even if not submitted")
    void testResolveOptionalFieldWithExistingAccountValue() {
        AccountSnapshot account = AccountSnapshot.builder()
                .tinNumber("12345678912")
                .tinCertificate("CP212-SR-4311f2ae-tinCertificate-image_cropper_1790747467886.jpg")
                .build();

        ServiceRequestConfigEntity config = ServiceRequestConfigEntity.builder()
                .serviceName("TIN_CHANGE")
                .fields(List.of(
                        FieldConfigEntity.builder().fieldName("tinNumber").accountFieldPath("tinNumber").required(true).build(),
                        FieldConfigEntity.builder().fieldName("tinCertificate").accountFieldPath("tinCertificate").required(false).file(true).build()
                ))
                .build();

        // User submits only tinNumber, not tinCertificate
        Map<String, Object> submittedFields = Map.of("tinNumber", "98765432100");
        Map<String, Object> oldValues = oldValueResolver.resolveOldValues(account, config, null, submittedFields);

        assertNotNull(oldValues);
        assertEquals("12345678912", oldValues.get("tinNumber"));
        assertEquals("CP212-SR-4311f2ae-tinCertificate-image_cropper_1790747467886.jpg", oldValues.get("tinCertificate"));
    }
}
