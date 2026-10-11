package com.beacepl.service_request_service.service.handler;

import com.beacepl.service_request_service.entity.FieldConfigEntity;
import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.enums.FieldDataType;
import com.beacepl.service_request_service.model.AccountSnapshot;
import com.beacepl.service_request_service.model.FieldChangeDetail;
import com.beacepl.service_request_service.service.support.ServiceRoleUtil;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles services whose old and new values are both supplied in the request payload
 * (config.valueSource = REQUEST), e.g. COMMISSION_CHANGE and MOBILE_APPS_REQUEST.
 * <p>
 * Field buckets are driven by FieldConfigEntity.valueRole:
 * OLD  -> oldValues[pairKey|fieldName]
 * NEW  -> newValues[pairKey|fieldName]
 * INFO -> newValues[fieldName]
 * fixedOldValue / fixedNewValue override submitted input.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestProvidedValuesHandler implements ServiceRequestHandler {

    public static final String ROLE_OLD = "OLD";
    public static final String ROLE_NEW = "NEW";
    public static final String ROLE_INFO = "INFO";
    public static final String ROW_CHANGE = "CHANGE";

    @Override
    public boolean supports(ServiceRequestConfigEntity config) {
        return ServiceRoleUtil.isRequestValueSource(config);
    }

    @Override
    public Map<String, Object> resolveOldValues(
            AccountSnapshot account,
            ServiceRequestConfigEntity config,
            String listItemIdentifierValue,
            Map<String, Object> submittedFields
    ) {
        Map<String, Object> oldValues = new LinkedHashMap<>();
        if (config.getFields() == null) return oldValues;

        for (FieldConfigEntity field : config.getFields()) {
            String key = key(field);
            if (field.getFixedOldValue() != null) {
                oldValues.put(key, field.getFixedOldValue());
            } else if (ROLE_OLD.equalsIgnoreCase(field.getValueRole())) {
                oldValues.put(key, submitted(submittedFields, field.getFieldName()));
            }
        }
        return oldValues;
    }

    @Override
    public Map<String, Object> resolveNewValues(
            Map<String, Object> submittedFields,
            Map<String, String> uploadedFileObjectNames,
            ServiceRequestConfigEntity config
    ) {
        Map<String, Object> newValues = new LinkedHashMap<>();
        if (config.getFields() == null) return newValues;

        for (FieldConfigEntity field : config.getFields()) {
            String role = role(field);
            if (ROLE_OLD.equals(role)) {
                continue;
            }
            String key = ROLE_NEW.equals(role) ? key(field) : field.getFieldName();
            Object value;
            if (field.getFixedNewValue() != null) {
                value = field.getFixedNewValue();
            } else if (field.isFile() && uploadedFileObjectNames != null) {
                value = uploadedFileObjectNames.get(field.getFieldName());
            } else {
                value = submitted(submittedFields, field.getFieldName());
            }
            if (value != null) {
                newValues.put(key, value);
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
        if (config == null || config.getFields() == null) return details;

        // Comparison rows first (OLD/NEW pairs), then INFO rows, so admin sees the change on top
        List<FieldChangeDetail> infoRows = new ArrayList<>();
        Set<String> emittedKeys = new HashSet<>();

        for (FieldConfigEntity field : config.getFields()) {
            String role = role(field);
            boolean isComparison = ROLE_OLD.equals(role) || ROLE_NEW.equals(role) || field.getFixedOldValue() != null;

            if (isComparison) {
                String key = key(field);
                if (!emittedKeys.add(key)) continue;
                FieldConfigEntity labelSource = findNewField(config, key, field);
                details.add(FieldChangeDetail.builder()
                        .fieldName(key)
                        .label(label(labelSource))
                        .dataType(dataType(labelSource))
                        .isFile(labelSource.isFile())
                        .oldValue(oldValues != null ? oldValues.get(key) : null)
                        .newValue(newValues != null ? newValues.get(key) : null)
                        .valueRole(ROW_CHANGE)
                        .build());
            } else {
                Object newVal = newValues != null ? newValues.get(field.getFieldName()) : null;
                if (newVal == null && !field.isRequired()) continue;
                infoRows.add(FieldChangeDetail.builder()
                        .fieldName(field.getFieldName())
                        .label(label(field))
                        .dataType(dataType(field))
                        .isFile(field.isFile() || field.getDataType() == FieldDataType.FILE)
                        .oldValue(null)
                        .newValue(newVal)
                        .valueRole(ROLE_INFO)
                        .build());
            }
        }
        details.addAll(infoRows);
        return details;
    }

    private FieldConfigEntity findNewField(ServiceRequestConfigEntity config, String key, FieldConfigEntity fallback) {
        for (FieldConfigEntity f : config.getFields()) {
            if (ROLE_NEW.equals(role(f)) && key.equals(key(f))) {
                return f;
            }
        }
        return fallback;
    }

    private static String role(FieldConfigEntity field) {
        return field.getValueRole() != null ? field.getValueRole().toUpperCase() : ROLE_INFO;
    }

    private static String key(FieldConfigEntity field) {
        return (field.getPairKey() != null && !field.getPairKey().isBlank()) ? field.getPairKey() : field.getFieldName();
    }

    private static String label(FieldConfigEntity field) {
        return (field.getLabel() != null && !field.getLabel().isBlank()) ? field.getLabel() : field.getFieldName();
    }

    private static String dataType(FieldConfigEntity field) {
        return field.getDataType() != null ? field.getDataType().name() : "TEXT";
    }

    private static Object submitted(Map<String, Object> submittedFields, String fieldName) {
        return submittedFields != null ? submittedFields.get(fieldName) : null;
    }
}
