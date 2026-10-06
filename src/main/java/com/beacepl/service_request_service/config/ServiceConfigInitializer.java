package com.beacepl.service_request_service.config;

import com.beacepl.service_request_service.entity.ApiConfig;
import com.beacepl.service_request_service.entity.ApprovalLevelConfig;
import com.beacepl.service_request_service.entity.FieldConfigEntity;
import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.enums.AudienceType;
import com.beacepl.service_request_service.enums.FieldDataType;
import com.beacepl.service_request_service.repository.ServiceRequestConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceConfigInitializer implements CommandLineRunner {

    private final ServiceRequestConfigRepository configRepository;

    @Override
    public void run(String... args) {
        log.info("Initializing Service Request Configurations...");

        createMobileChangeConfig();
        createEmailChangeConfig();
        createAddressChangeConfig();
        createTinChangeConfig();
        createBankChangeConfig();
        createNomineeAddConfig();
        createNomineeEditConfig();

        log.info("Service Request Configurations initialization complete.");
    }

    private void createMobileChangeConfig() {
        if (configRepository.findByServiceName("MOBILE_CHANGE").isEmpty()) {
            ServiceRequestConfigEntity config = ServiceRequestConfigEntity.builder()
                    .serviceName("MOBILE_CHANGE")
                    .displayName("Change Mobile Number")
                    .description("Request to update your primary account mobile number")
                    .active(true)
                    .multipart(false)
                    .listBased(false)
                    .allowedActions(List.of("EDIT"))
                    .section("PERSONAL_DETAILS")
                    .sectionDisplayName("Personal Details")
                    .sectionOrder(1)
                    .displayOrder(1)
                    .targetAudience(AudienceType.BOTH)
                    .fields(List.of(
                            FieldConfigEntity.builder()
                                    .fieldName("newMobileNumber")
                                    .label("New Mobile Number")
                                    .dataType(FieldDataType.NUMBER)
                                    .required(true)
                                    .accountFieldPath("mobileNumber")
                                    .file(false)
                                    .build()
                    ))
                    .apiConfig(ApiConfig.builder()
                            .targetUrl("http://10.20.242.239:9092/api/v1/accounts/{accountId}/mobile")
                            .httpMethod("PUT")
                            .headers(Map.of("Content-Type", "application/json"))
                            .pathParams(List.of("accountId"))
                            .bodyTemplate(Map.of("mobileNumber", "${fieldValues.newMobileNumber}"))
                            .build())
                    .approvalLevels(List.of(
                            ApprovalLevelConfig.builder()
                                    .level(1)
                                    .levelName("Branch RM Review")
                                    .allowedRoles(List.of("ROLE_RM", "ROLE_BRANCH_MANAGER"))
                                    .build()
                    ))
                    .build();
            configRepository.save(config);
            log.info("Seeded configuration: MOBILE_CHANGE");
        }
    }

    private void createEmailChangeConfig() {
        if (configRepository.findByServiceName("EMAIL_CHANGE").isEmpty()) {
            ServiceRequestConfigEntity config = ServiceRequestConfigEntity.builder()
                    .serviceName("EMAIL_CHANGE")
                    .displayName("Change Email Address")
                    .description("Request to update your registered email address")
                    .active(true)
                    .multipart(false)
                    .listBased(false)
                    .allowedActions(List.of("EDIT"))
                    .section("PERSONAL_DETAILS")
                    .sectionDisplayName("Personal Details")
                    .sectionOrder(1)
                    .displayOrder(2)
                    .targetAudience(AudienceType.BOTH)
                    .fields(List.of(
                            FieldConfigEntity.builder()
                                    .fieldName("newEmailAddress")
                                    .label("New Email Address")
                                    .dataType(FieldDataType.EMAIL)
                                    .required(true)
                                    .accountFieldPath("emailAddress")
                                    .file(false)
                                    .build()
                    ))
                    .apiConfig(ApiConfig.builder()
                            .targetUrl("http://10.20.242.239:9092/api/v1/accounts/{accountId}/email")
                            .httpMethod("PUT")
                            .headers(Map.of("Content-Type", "application/json"))
                            .pathParams(List.of("accountId"))
                            .bodyTemplate(Map.of("emailAddress", "${fieldValues.newEmailAddress}"))
                            .build())
                    .approvalLevels(List.of(
                            ApprovalLevelConfig.builder()
                                    .level(1)
                                    .levelName("Branch RM Review")
                                    .allowedRoles(List.of("ROLE_RM", "ROLE_BRANCH_MANAGER"))
                                    .build()
                    ))
                    .build();
            configRepository.save(config);
            log.info("Seeded configuration: EMAIL_CHANGE");
        }
    }

    private void createAddressChangeConfig() {
        if (configRepository.findByServiceName("ADDRESS_CHANGE").isEmpty()) {
            ServiceRequestConfigEntity config = ServiceRequestConfigEntity.builder()
                    .serviceName("ADDRESS_CHANGE")
                    .displayName("Change Address")
                    .description("Request to update your residential address details")
                    .active(true)
                    .multipart(false)
                    .listBased(false)
                    .allowedActions(List.of("EDIT"))
                    .section("CONTACT_INFO")
                    .sectionDisplayName("Contact Information")
                    .sectionOrder(2)
                    .displayOrder(1)
                    .targetAudience(AudienceType.BOTH)
                    .fields(List.of(
                            FieldConfigEntity.builder().fieldName("addressLine1").label("Address Line 1").dataType(FieldDataType.TEXT).required(true).accountFieldPath("addressLine1").file(false).build(),
                            FieldConfigEntity.builder().fieldName("addressLine2").label("Address Line 2").dataType(FieldDataType.TEXT).required(false).accountFieldPath("addressLine2").file(false).build(),
                            FieldConfigEntity.builder().fieldName("city").label("City").dataType(FieldDataType.TEXT).required(true).accountFieldPath("city").file(false).build(),
                            FieldConfigEntity.builder().fieldName("state").label("State").dataType(FieldDataType.TEXT).required(true).accountFieldPath("state").file(false).build(),
                            FieldConfigEntity.builder().fieldName("zipCode").label("Zip Code").dataType(FieldDataType.TEXT).required(true).accountFieldPath("zipCode").file(false).build(),
                            FieldConfigEntity.builder().fieldName("country").label("Country").dataType(FieldDataType.TEXT).required(true).accountFieldPath("country").file(false).build()
                    ))
                    .apiConfig(ApiConfig.builder()
                            .targetUrl("http://10.20.242.239:9092/api/v1/accounts/{accountId}/address")
                            .httpMethod("PUT")
                            .headers(Map.of("Content-Type", "application/json"))
                            .pathParams(List.of("accountId"))
                            .build())
                    .approvalLevels(List.of(
                            ApprovalLevelConfig.builder()
                                    .level(1)
                                    .levelName("RM Initial Verification")
                                    .allowedRoles(List.of("ROLE_RM"))
                                    .build(),
                            ApprovalLevelConfig.builder()
                                    .level(2)
                                    .levelName("Compliance Head Approval")
                                    .allowedRoles(List.of("ROLE_COMPLIANCE", "ROLE_ADMIN"))
                                    .build()
                    ))
                    .build();
            configRepository.save(config);
            log.info("Seeded configuration: ADDRESS_CHANGE");
        }
    }

    private void createTinChangeConfig() {
        if (configRepository.findByServiceName("TIN_CHANGE").isEmpty()) {
            ServiceRequestConfigEntity config = ServiceRequestConfigEntity.builder()
                    .serviceName("TIN_CHANGE")
                    .displayName("Change TIN Information")
                    .description("Add or update your Tax Identification Number and certificate")
                    .active(true)
                    .multipart(true)
                    .listBased(false)
                    .allowedActions(List.of("ADD", "EDIT"))
                    .section("TAX_INFO")
                    .sectionDisplayName("Tax Information")
                    .sectionOrder(3)
                    .displayOrder(1)
                    .targetAudience(AudienceType.BOTH)
                    .fields(List.of(
                            FieldConfigEntity.builder().fieldName("tinNumber").label("TIN Number").dataType(FieldDataType.STRING).required(true).accountFieldPath("tinNumber").file(false).build(),
                            FieldConfigEntity.builder().fieldName("tinCertificate").label("TIN Certificate Document").dataType(FieldDataType.FILE).required(false).accountFieldPath("tinCertificate").file(true).build()
                    ))
                    .apiConfig(ApiConfig.builder()
                            .targetUrl("http://10.20.242.239:9092/api/v1/accounts/{accountId}/tin")
                            .httpMethod("PUT")
                            .headers(Map.of("Content-Type", "application/json"))
                            .pathParams(List.of("accountId"))
                            .build())
                    .approvalLevels(List.of(
                            ApprovalLevelConfig.builder()
                                    .level(1)
                                    .levelName("Branch Operations Review")
                                    .allowedRoles(List.of("ROLE_OPS", "ROLE_RM"))
                                    .build(),
                            ApprovalLevelConfig.builder()
                                    .level(2)
                                    .levelName("Tax Compliance Approval")
                                    .allowedRoles(List.of("ROLE_COMPLIANCE", "ROLE_ADMIN"))
                                    .build()
                    ))
                    .build();
            configRepository.save(config);
            log.info("Seeded configuration: TIN_CHANGE");
        }
    }

    private void createBankChangeConfig() {
        if (configRepository.findByServiceName("BANK_CHANGE").isEmpty()) {
            ServiceRequestConfigEntity config = ServiceRequestConfigEntity.builder()
                    .serviceName("BANK_CHANGE")
                    .displayName("Change Bank Account")
                    .description("Update bank account number, bank name, routing number, or branch")
                    .active(true)
                    .multipart(true)
                    .listBased(false)
                    .allowedActions(List.of("EDIT"))
                    .section("BANK_INFO")
                    .sectionDisplayName("Bank Information")
                    .sectionOrder(4)
                    .displayOrder(1)
                    .targetAudience(AudienceType.BOTH)
                    .fields(List.of(
                            FieldConfigEntity.builder().fieldName("bankAccountNumber").label("Bank Account Number").dataType(FieldDataType.STRING).required(true).accountFieldPath("bankAccountNumber").file(false).build(),
                            FieldConfigEntity.builder().fieldName("bankName").label("Bank Name").dataType(FieldDataType.TEXT).required(true).accountFieldPath("bankName").file(false).build(),
                            FieldConfigEntity.builder().fieldName("branchName").label("Branch Name").dataType(FieldDataType.TEXT).required(true).accountFieldPath("branchName").file(false).build(),
                            FieldConfigEntity.builder().fieldName("routingNumber").label("Routing Number").dataType(FieldDataType.STRING).required(true).accountFieldPath("routingNumber").file(false).build(),
                            FieldConfigEntity.builder().fieldName("chequeLeaf").label("Cheque Leaf Document").dataType(FieldDataType.FILE).required(false).accountFieldPath("chequeLeaf").file(true).build()
                    ))
                    .apiConfig(ApiConfig.builder()
                            .targetUrl("http://10.20.242.239:9092/api/v1/accounts/{accountId}/bank")
                            .httpMethod("PUT")
                            .headers(Map.of("Content-Type", "application/json"))
                            .pathParams(List.of("accountId"))
                            .build())
                    .approvalLevels(List.of(
                            ApprovalLevelConfig.builder()
                                    .level(1)
                                    .levelName("Branch Manager Review")
                                    .allowedRoles(List.of("ROLE_BRANCH_MANAGER", "ROLE_RM"))
                                    .build(),
                            ApprovalLevelConfig.builder()
                                    .level(2)
                                    .levelName("Settlement Accounts Head Approval")
                                    .allowedRoles(List.of("ROLE_SETTLEMENT", "ROLE_ADMIN"))
                                    .build()
                    ))
                    .build();
            configRepository.save(config);
            log.info("Seeded configuration: BANK_CHANGE");
        }
    }

    private void createNomineeAddConfig() {
        if (configRepository.findByServiceName("NOMINEE_ADD").isEmpty()) {
            ServiceRequestConfigEntity config = ServiceRequestConfigEntity.builder()
                    .serviceName("NOMINEE_ADD")
                    .displayName("Add Nominee")
                    .description("Add a new nominee to your account")
                    .active(true)
                    .multipart(true)
                    .listBased(true)
                    .targetListField("nominees")
                    .listIdentifierField("nid")
                    .allowedActions(List.of("ADD"))
                    .section("NOMINEE")
                    .sectionDisplayName("Nominee Information")
                    .sectionOrder(5)
                    .displayOrder(1)
                    .targetAudience(AudienceType.BOTH)
                    .fields(getNomineeFields())
                    .apiConfig(ApiConfig.builder()
                            .targetUrl("http://10.20.242.239:9092/api/v1/accounts/{accountId}/nominees")
                            .httpMethod("POST")
                            .headers(Map.of("Content-Type", "application/json"))
                            .pathParams(List.of("accountId"))
                            .build())
                    .approvalLevels(List.of(
                            ApprovalLevelConfig.builder()
                                    .level(1)
                                    .levelName("Branch RM Review")
                                    .allowedRoles(List.of("ROLE_RM", "ROLE_BRANCH_MANAGER"))
                                    .build(),
                            ApprovalLevelConfig.builder()
                                    .level(2)
                                    .levelName("Central Operations Approval")
                                    .allowedRoles(List.of("ROLE_OPS", "ROLE_ADMIN"))
                                    .build()
                    ))
                    .build();
            configRepository.save(config);
            log.info("Seeded configuration: NOMINEE_ADD");
        }
    }

    private void createNomineeEditConfig() {
        if (configRepository.findByServiceName("NOMINEE_EDIT").isEmpty()) {
            ServiceRequestConfigEntity config = ServiceRequestConfigEntity.builder()
                    .serviceName("NOMINEE_EDIT")
                    .displayName("Edit Nominee")
                    .description("Edit an existing nominee's details")
                    .active(true)
                    .multipart(true)
                    .listBased(true)
                    .targetListField("nominees")
                    .listIdentifierField("nid")
                    .allowedActions(List.of("EDIT"))
                    .section("NOMINEE")
                    .sectionDisplayName("Nominee Information")
                    .sectionOrder(5)
                    .displayOrder(2)
                    .targetAudience(AudienceType.BOTH)
                    .fields(getNomineeFields())
                    .apiConfig(ApiConfig.builder()
                            .targetUrl("http://10.20.242.239:9092/api/v1/accounts/{accountId}/nominees")
                            .httpMethod("PUT")
                            .headers(Map.of("Content-Type", "application/json"))
                            .pathParams(List.of("accountId"))
                            .build())
                    .approvalLevels(List.of(
                            ApprovalLevelConfig.builder()
                                    .level(1)
                                    .levelName("Branch RM Review")
                                    .allowedRoles(List.of("ROLE_RM", "ROLE_BRANCH_MANAGER"))
                                    .build(),
                            ApprovalLevelConfig.builder()
                                    .level(2)
                                    .levelName("Central Operations Approval")
                                    .allowedRoles(List.of("ROLE_OPS", "ROLE_ADMIN"))
                                    .build()
                    ))
                    .build();
            configRepository.save(config);
            log.info("Seeded configuration: NOMINEE_EDIT");
        }
    }

    private List<FieldConfigEntity> getNomineeFields() {
        List<FieldConfigEntity> fields = new ArrayList<>();
        fields.add(FieldConfigEntity.builder().fieldName("name").label("Nominee Name").dataType(FieldDataType.TEXT).required(true).accountFieldPath("name").file(false).build());
        fields.add(FieldConfigEntity.builder().fieldName("relation").label("Relation").dataType(FieldDataType.TEXT).required(true).accountFieldPath("relation").file(false).build());
        fields.add(FieldConfigEntity.builder().fieldName("nid").label("Nominee NID Number").dataType(FieldDataType.STRING).required(true).accountFieldPath("nid").file(false).build());
        fields.add(FieldConfigEntity.builder().fieldName("percentage").label("Percentage Share").dataType(FieldDataType.NUMBER).required(true).accountFieldPath("percentage").file(false).build());
        fields.add(FieldConfigEntity.builder().fieldName("mobileNumber").label("Mobile Number").dataType(FieldDataType.NUMBER).required(false).accountFieldPath("mobileNumber").file(false).build());
        fields.add(FieldConfigEntity.builder().fieldName("address").label("Address").dataType(FieldDataType.TEXT).required(false).accountFieldPath("address").file(false).build());
        fields.add(FieldConfigEntity.builder().fieldName("city").label("City").dataType(FieldDataType.TEXT).required(false).accountFieldPath("city").file(false).build());
        fields.add(FieldConfigEntity.builder().fieldName("state").label("State").dataType(FieldDataType.TEXT).required(false).accountFieldPath("state").file(false).build());
        fields.add(FieldConfigEntity.builder().fieldName("zipCode").label("Zip Code").dataType(FieldDataType.TEXT).required(false).accountFieldPath("zipCode").file(false).build());
        fields.add(FieldConfigEntity.builder().fieldName("country").label("Country").dataType(FieldDataType.TEXT).required(false).accountFieldPath("country").file(false).build());
        fields.add(FieldConfigEntity.builder().fieldName("residency").label("Residency").dataType(FieldDataType.TEXT).required(false).accountFieldPath("residency").file(false).build());

        // Files
        fields.add(FieldConfigEntity.builder().fieldName("nomineeNidFront").label("Nominee NID Front Photo").dataType(FieldDataType.FILE).required(false).accountFieldPath("nomineeNidFront").file(true).build());
        fields.add(FieldConfigEntity.builder().fieldName("nomineeNidBack").label("Nominee NID Back Photo").dataType(FieldDataType.FILE).required(false).accountFieldPath("nomineeNidBack").file(true).build());
        fields.add(FieldConfigEntity.builder().fieldName("nomineePhoto").label("Nominee Passport Photo").dataType(FieldDataType.FILE).required(false).accountFieldPath("nomineePhoto").file(true).build());
        fields.add(FieldConfigEntity.builder().fieldName("nomineeSignature").label("Nominee Signature").dataType(FieldDataType.FILE).required(false).accountFieldPath("nomineeSignature").file(true).build());

        // Guardian fields
        fields.add(FieldConfigEntity.builder().fieldName("minor").label("Is Minor").dataType(FieldDataType.BOOLEAN).required(false).accountFieldPath("minor").file(false).build());
        fields.add(FieldConfigEntity.builder().fieldName("guardianName").label("Guardian Name").dataType(FieldDataType.TEXT).required(false).accountFieldPath("guardianName").file(false).build());
        fields.add(FieldConfigEntity.builder().fieldName("relationshipWithNominee").label("Relationship with Nominee").dataType(FieldDataType.TEXT).required(false).accountFieldPath("relationshipWithNominee").file(false).build());
        fields.add(FieldConfigEntity.builder().fieldName("guardianNidNumber").label("Guardian NID Number").dataType(FieldDataType.STRING).required(false).accountFieldPath("guardianNidNumber").file(false).build());
        fields.add(FieldConfigEntity.builder().fieldName("guardianNidFront").label("Guardian NID Front").dataType(FieldDataType.FILE).required(false).accountFieldPath("guardianNidFront").file(true).build());
        fields.add(FieldConfigEntity.builder().fieldName("guardianNidBack").label("Guardian NID Back").dataType(FieldDataType.FILE).required(false).accountFieldPath("guardianNidBack").file(true).build());
        fields.add(FieldConfigEntity.builder().fieldName("guardianSignature").label("Guardian Signature").dataType(FieldDataType.FILE).required(false).accountFieldPath("guardianSignature").file(true).build());

        return fields;
    }
}
