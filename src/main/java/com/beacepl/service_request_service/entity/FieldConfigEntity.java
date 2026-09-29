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

    private String validationRegex;

    private Integer minLength;

    private Integer maxLength;

    private String accountFieldPath;

    private boolean file;
}
