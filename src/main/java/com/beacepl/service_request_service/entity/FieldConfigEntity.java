package com.beacepl.service_request_service.entity;

import com.beacepl.service_request_service.enums.FieldDataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Describes a single field belonging to a ServiceRequestConfigEntity.
 * This is the piece that makes the whole flow dynamic: everything the
 * controller/service needs to know about a field (is it required, is it a
 * file, what regex should it match, and - critically - where does it live
 * on the AccountEntity) is data, not code.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldConfigEntity {

    /** Key used in the incoming request payload, e.g. "bankAccountNumber" */
    private String fieldName;

    /** Human readable label, used when building validation error messages */
    private String label;

    private FieldDataType dataType;

    private boolean required;

    /** Optional regex the raw string value must match (ignored for FILE fields) */
    private String validationRegex;

    private Integer minLength;

    private Integer maxLength;

    /**
     * Dot-notation path into AccountEntity that this field maps to, e.g.
     * "mobileNumber", "bank.accountNo", "tinNumber". For fields that belong
     * to a list item (e.g. a nominees name) this is the path *within the
     * list item object*, e.g. "name", "guardianNidFront".
     */
    private String accountFieldPath;

    /**
     * If true, this field is expected to arrive as a MultipartFile part.
     * The stored value (a file path/URL, produced by FileStorageService)
     * is what ultimately gets written to accountFieldPath.
     */
    private boolean file;
}
