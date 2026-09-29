package com.beacepl.service_request_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldChangeDetail {

    /** Technical field key, e.g. "newMobileNumber", "tinCertificate", "nomineeNidFront" */
    private String fieldName;

    /** Human-readable display label for frontend, e.g. "Mobile Number", "TIN Certificate" */
    private String label;

    /** Data type string: "TEXT", "NUMBER", "EMAIL", "DATE", "BOOLEAN", "FILE" */
    private String dataType;

    /** Flag indicating whether this field represents a file upload */
    private boolean isFile;

    /** Stored old value (presigned URL for file fields when returned to admin) */
    private Object oldValue;

    /** Stored requested new value (presigned URL for file fields when returned to admin) */
    private Object newValue;
}
