package com.beacepl.service_request_service.model;

import com.beacepl.service_request_service.enums.FieldDataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceConfigAndOldValuesResponseDto {

    private String serviceName;
    private String displayName;
    private String description;
    private String section;
    private String sectionDisplayName;
    private boolean isListBased;
    private String targetListField;
    private String listIdentifierField;
    private List<String> allowedActions;

    private List<FieldConfigWithValueDto> fields;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldConfigWithValueDto {
        private String fieldName;
        private String label;
        private FieldDataType dataType;
        private String valueType;
        private boolean required;
        private Integer minLength;
        private Integer maxLength;
        private String accountFieldPath;
        private boolean isFile;
        private Object oldValue;
    }
}
