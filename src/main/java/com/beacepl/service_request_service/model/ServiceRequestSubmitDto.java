package com.beacepl.service_request_service.model;

import com.beacepl.service_request_service.enums.RequestAction;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * This is the JSON part of the multipart request (part name: "requestData").
 * Files are sent as additional multipart parts, one per FILE field, named
 * exactly after the corresponding FieldConfigEntity.fieldName - e.g. a
 * NOMINEE_ADD request would include parts "nomineeNidFront",
 * "nomineeNidBack", "nomineePhoto", "nomineeSignature" alongside requestData.
 *
 * Example requestData JSON for a NOMINEE_ADD request:
 * {
 *   "serviceName": "NOMINEE_ADD",
 *   "accountId": "665f1...",
 *   "mobileNumber": "01700000000",
 *   "action": "ADD",
 *   "fieldValues": { "name": "John Doe", "relation": "Father", "nid": "1234567890", "percentage": 50 }
 * }
 *
 * Example requestData JSON for a plain MOBILE_CHANGE request (no files, can
 * be sent as application/json directly to the same endpoint):
 * {
 *   "serviceName": "MOBILE_CHANGE",
 *   "accountId": "665f1...",
 *   "mobileNumber": "01700000000",
 *   "action": "EDIT",
 *   "fieldValues": { "newMobileNumber": "01800000000" }
 * }
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceRequestSubmitDto {

    private String serviceName;

    private String accountId;

    /** Mobile number of the requesting customer, used for lookups/notifications */
    private String mobileNumber;
    private String emailAddress;
    private String investorCode;
    private String otp;

    private RequestAction action;

    /** Non-file field values, keyed by FieldConfigEntity.fieldName */
    private Map<String, Object> fieldValues;
    private Map<String, String> fieldTypes;
    private Map<String, Object> oldValues;

    /**
     * Required for EDIT/REMOVE on a listBased service (e.g. which nominee,
     * identified by the configured listIdentifierField value, e.g. an NID).
     */
    private String listItemIdentifierValue;
}
