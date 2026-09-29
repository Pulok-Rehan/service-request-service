package com.beacepl.service_request_service.service.handler;

import com.beacepl.service_request_service.entity.FieldConfigEntity;
import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.enums.FieldDataType;
import com.beacepl.service_request_service.model.AccountSnapshot;
import com.beacepl.service_request_service.model.FieldChangeDetail;
import com.beacepl.service_request_service.service.impl.OldValueResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListBasedRequestHandler implements ServiceRequestHandler {

    private final OldValueResolver oldValueResolver;

    @Override
    public boolean supports(ServiceRequestConfigEntity config) {
        return config != null && config.isListBased();
    }

    @Override
    public Map<String, Object> resolveOldValues(
            AccountSnapshot account,
            ServiceRequestConfigEntity config,
            String listItemIdentifierValue,
            Map<String, Object> submittedFields
    ) {
        return oldValueResolver.resolveListBasedOldValues(account, config, listItemIdentifierValue);
    }

    @Override
    public Map<String, Object> resolveNewValues(
            Map<String, Object> submittedFields,
            Map<String, String> uploadedFileObjectNames,
            ServiceRequestConfigEntity config
    ) {
        Map<String, Object> newValues = new HashMap<>();

        if (submittedFields != null) {
            newValues.putAll(submittedFields);
        }

        if (config.getFields() != null) {
            for (FieldConfigEntity field : config.getFields()) {
                if (field.isFile() && uploadedFileObjectNames != null && uploadedFileObjectNames.containsKey(field.getFieldName())) {
                    newValues.put(field.getFieldName(), uploadedFileObjectNames.get(field.getFieldName()));
                }
            }
        }

        return newValues;
    }

    @Override
    public List<FieldChangeDetail> buildFieldDetails(
            Map<String, Object> oldValues,
            Map<String, Object> newValues,
            ServiceRequestConfigEntity config
    ) {
        List<FieldChangeDetail> details = new ArrayList<>();
        if (config == null || config.getFields() == null) {
            return details;
        }

        for (FieldConfigEntity field : config.getFields()) {
            boolean isFile = field.isFile() || field.getDataType() == FieldDataType.FILE;
            String fieldName = field.getFieldName();
            String label = (field.getLabel() != null && !field.getLabel().isBlank()) ? field.getLabel() : fieldName;
            String dataType = field.getDataType() != null ? field.getDataType().name() : "TEXT";

            Object oldVal = null;
            if (oldValues != null) {
                String leafPath = field.getAccountFieldPath() != null && field.getAccountFieldPath().contains(".")
                        ? field.getAccountFieldPath().substring(field.getAccountFieldPath().lastIndexOf('.') + 1)
                        : field.getAccountFieldPath();
                if (leafPath != null && oldValues.containsKey(leafPath)) {
                    oldVal = oldValues.get(leafPath);
                } else if (oldValues.containsKey(fieldName)) {
                    oldVal = oldValues.get(fieldName);
                }
            }

            Object newVal = (newValues != null) ? newValues.get(fieldName) : null;

            if (newVal != null || oldVal != null || field.isRequired()) {
                details.add(FieldChangeDetail.builder()
                        .fieldName(fieldName)
                        .label(label)
                        .dataType(dataType)
                        .isFile(isFile)
                        .oldValue(oldVal)
                        .newValue(newVal)
                        .build());
            }
        }

        return details;
    }
}
