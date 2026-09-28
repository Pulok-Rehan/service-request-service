package com.beacepl.service_request_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Sent from service-request-service to dbp-onboarding-service once an
 * admin approves a change request. accountService applies this generically
 * against AccountEntity - it has no idea what "MOBILE_CHANGE" or
 * "NOMINEE_ADD" even are, it just applies dot-path field updates and list
 * operations.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountPatchRequestDto {

    private String accountId;

    /** dot-path (into AccountEntity) -> new value, e.g. "bank.accountNo" -> "0123456789" */
    private Map<String, Object> fieldUpdates;

    /** operations against list fields, e.g. nominees */
    private List<AccountFieldUpdateDto> listUpdates;

    /** which change request triggered this, for traceability/audit on the account service side */
    private String sourceChangeRequestId;
}
