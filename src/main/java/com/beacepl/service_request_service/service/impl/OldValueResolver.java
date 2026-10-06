package com.beacepl.service_request_service.service.impl;

import com.beacepl.service_request_service.entity.FieldConfigEntity;
import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.model.AccountSnapshot;
import com.beacepl.service_request_service.model.NomineeEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class OldValueResolver {

    private final ObjectMapper objectMapper;

    /**
     * Resolves old values from an account snapshot based on service configuration metadata.
     */
    public Map<String, Object> resolveOldValues(
            AccountSnapshot account,
            ServiceRequestConfigEntity config,
            String listItemIdentifierValue,
            Map<String, Object> submittedFields
    ) {
        if (account == null || config == null || config.getFields() == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> accountMap = objectMapper.convertValue(account, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> oldValues = new HashMap<>();

        if (config.isListBased()) {
            return resolveListBasedOldValues(account, config, listItemIdentifierValue);
        }

        for (FieldConfigEntity fieldConfig : config.getFields()) {
            // If field is optional and not submitted by user, skip resolving it unless it's required
            if (!fieldConfig.isRequired() && (submittedFields != null && !submittedFields.containsKey(fieldConfig.getFieldName()))) {
                continue;
            }

            String fieldPath = fieldConfig.getAccountFieldPath();
            if (fieldPath == null || fieldPath.isBlank()) {
                fieldPath = fieldConfig.getFieldName();
            }

            Object resolvedValue = resolveNestedValue(accountMap, fieldPath);
            oldValues.put(fieldPath, resolvedValue);
        }

        return oldValues;
    }

    /**
     * Resolves old values for a list-based item (e.g. Nominee EDIT).
     */
    public Map<String, Object> resolveListBasedOldValues(
            AccountSnapshot account,
            ServiceRequestConfigEntity config,
            String listItemIdentifierValue
    ) {
        if (account == null || config == null || !config.isListBased()) {
            return null;
        }

        String targetListField = config.getTargetListField(); // e.g. "nominees"
        String identifierField = config.getListIdentifierField(); // e.g. "nid"

        if ("nominees".equalsIgnoreCase(targetListField) && account.getNominees() != null && !account.getNominees().isEmpty()) {
            NomineeEntity targetNominee = null;

            if (listItemIdentifierValue != null && !listItemIdentifierValue.isBlank()) {
                for (NomineeEntity nominee : account.getNominees()) {
                    String nomineeIdentifier = getNomineeIdentifierValue(nominee, identifierField);
                    if (Objects.equals(nomineeIdentifier, listItemIdentifierValue)
                            || Objects.equals(nominee.getNid(), listItemIdentifierValue)
                            || Objects.equals(nominee.getId(), listItemIdentifierValue)
                            || Objects.equals(nominee.getName(), listItemIdentifierValue)) {
                        targetNominee = nominee;
                        break;
                    }
                }
            } else {
                // If listItemIdentifierValue is not provided, default to the first nominee
                targetNominee = account.getNominees().get(0);
            }

            if (targetNominee != null) {
                Map<String, Object> nomineeMap = objectMapper.convertValue(targetNominee, new TypeReference<Map<String, Object>>() {});
                Map<String, Object> oldValues = new HashMap<>();
                for (FieldConfigEntity fieldConfig : config.getFields()) {
                    String path = fieldConfig.getAccountFieldPath();
                    if (path == null || path.isBlank()) {
                        path = fieldConfig.getFieldName();
                    }
                    String leafPath = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    Object val = nomineeMap.get(leafPath);
                    if (val == null) {
                        val = nomineeMap.get(fieldConfig.getFieldName());
                    }
                    oldValues.put(fieldConfig.getFieldName(), val);
                    oldValues.put(leafPath, val);
                }
                return oldValues;
            }
        }

        log.warn("Matching list item not found for targetListField: {}, identifierField: {}, value: {}",
                targetListField, identifierField, listItemIdentifierValue);
        return null;
    }

    /**
     * Navigates dot-separated nested path (e.g. "bank.accountNo", "addressLine1").
     */
    public Object resolveNestedValue(Map<String, Object> rootMap, String path) {
        if (rootMap == null || path == null || path.isBlank()) {
            return null;
        }

        String[] parts = path.split("\\.");
        Object current = rootMap;

        for (String part : parts) {
            if (current instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) current;
                current = map.get(part);
            } else {
                return null;
            }
        }

        return current;
    }

    private String getNomineeIdentifierValue(NomineeEntity nominee, String identifierField) {
        if (nominee == null) return null;
        if ("nid".equalsIgnoreCase(identifierField)) return nominee.getNid();
        if ("name".equalsIgnoreCase(identifierField)) return nominee.getName();
        if ("id".equalsIgnoreCase(identifierField)) return nominee.getId();
        if ("mobileNumber".equalsIgnoreCase(identifierField)) return nominee.getMobileNumber();
        return nominee.getNid();
    }
}
