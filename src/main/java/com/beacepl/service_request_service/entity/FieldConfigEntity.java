package com.beacepl.service_request_service.entity;

import com.beacepl.service_request_service.enums.FieldDataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldConfigEntity {

    private String fieldName;

    private String label;

    private FieldDataType dataType;

    private boolean required;

    private Integer minLength;

    private Integer maxLength;

    private String accountFieldPath;

    private boolean file;

    /**
     * Only used when the service valueSource is REQUEST.
     * OLD  - submitted value is the current/existing value (goes to oldValues)
     * NEW  - submitted value is the proposed value (goes to newValues)
     * INFO - contextual information shown to admin (goes to newValues, no old counterpart)
     */
    private String valueRole;

    /** Shared key that links an OLD field and a NEW field into one comparison row (e.g. "commissionRate"). */
    private String pairKey;

    /** Fixed old value stored regardless of input (e.g. mobileAppAccess=false). */
    private Object fixedOldValue;

    /** Fixed new value stored regardless of input (e.g. mobileAppAccess=true). */
    private Object fixedNewValue;

    /** Hint for frontend: display only, user does not enter this field. */
    private boolean readOnly;
}
