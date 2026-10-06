package com.beacepl.service_request_service.service.impl;

import com.beacepl.service_request_service.entity.FieldConfigEntity;
import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.enums.FieldDataType;
import com.beacepl.service_request_service.exceptions.InvalidRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@Slf4j
public class FieldValidationService {

    public void validateServiceAndFields(
            ServiceRequestConfigEntity config,
            String action,
            String listItemIdentifierValue,
            Map<String, Object> submittedFields,
            Map<String, MultipartFile> uploadedFiles
    ) {
        if (config == null) {
            throw new InvalidRequestException("Service configuration not found.");
        }

        if (!config.isActive()) {
            throw new InvalidRequestException("Service " + config.getServiceName() + " is currently inactive.");
        }

        // Validate allowed actions
        if (config.getAllowedActions() != null && !config.getAllowedActions().isEmpty()) {
            boolean actionAllowed = config.getAllowedActions().stream()
                    .anyMatch(a -> a.equalsIgnoreCase(action));
            if (!actionAllowed) {
                throw new InvalidRequestException("Action '" + action + "' is not allowed for service " + config.getServiceName() + ". Allowed: " + config.getAllowedActions());
            }
        }

        // Validate list identifier if listBased and EDIT
        if (config.isListBased() && "EDIT".equalsIgnoreCase(action)) {
            if (listItemIdentifierValue == null || listItemIdentifierValue.isBlank()) {
                throw new InvalidRequestException("List item identifier ('" + config.getListIdentifierField() + "') is required when editing a list item.");
            }
        }

        if (config.getFields() == null || config.getFields().isEmpty()) {
            return;
        }

        for (FieldConfigEntity field : config.getFields()) {
            validateSingleField(field, submittedFields, uploadedFiles, action);
        }

        // Special business validation for Nominee minor / guardian requirements
        validateNomineeMinorGuardianRules(submittedFields, uploadedFiles);
    }

    private void validateSingleField(
            FieldConfigEntity field,
            Map<String, Object> submittedFields,
            Map<String, MultipartFile> uploadedFiles,
            String action
    ) {
        String fieldName = field.getFieldName();
        String label = (field.getLabel() != null && !field.getLabel().isBlank()) ? field.getLabel() : fieldName;

        if (field.isFile() || field.getDataType() == FieldDataType.FILE) {
            MultipartFile file = (uploadedFiles != null) ? uploadedFiles.get(fieldName) : null;
            if (field.isRequired() && "ADD".equalsIgnoreCase(action)) {
                if (file == null || file.isEmpty()) {
                    throw new InvalidRequestException("File for field '" + label + "' (" + fieldName + ") is required.");
                }
            }
            if (file != null && !file.isEmpty()) {
                validateFileConstraints(fieldName, file);
            }
            return;
        }

        Object value = (submittedFields != null) ? submittedFields.get(fieldName) : null;

        // Required check
        if (field.isRequired()) {
            if (value == null || value.toString().trim().isEmpty()) {
                throw new InvalidRequestException("Field '" + label + "' is required.");
            }
        }

        if (value == null || value.toString().trim().isEmpty()) {
            return;
        }

        String valueStr = value.toString().trim();

        // Data Type Validation
        validateDataType(label, field.getDataType(), valueStr);

        // Length validation
        if (field.getMinLength() != null && valueStr.length() < field.getMinLength()) {
            throw new InvalidRequestException("Field '" + label + "' must be at least " + field.getMinLength() + " characters.");
        }
        if (field.getMaxLength() != null && valueStr.length() > field.getMaxLength()) {
            throw new InvalidRequestException("Field '" + label + "' cannot exceed " + field.getMaxLength() + " characters.");
        }
    }

    private void validateDataType(String label, FieldDataType dataType, String valueStr) {
        if (dataType == null) return;
        switch (dataType) {
            case NUMBER -> {
                if (!valueStr.matches("^\\d+$")) {
                    throw new InvalidRequestException("Field '" + label + "' must be a valid numeric integer value.");
                }
            }
            case FLOAT, DECIMAL -> {
                try {
                    Double.parseDouble(valueStr);
                } catch (NumberFormatException e) {
                    throw new InvalidRequestException("Field '" + label + "' must be a valid number.");
                }
            }
            case BOOLEAN -> {
                if (!"true".equalsIgnoreCase(valueStr) && !"false".equalsIgnoreCase(valueStr)) {
                    throw new InvalidRequestException("Field '" + label + "' must be true or false.");
                }
            }
            case EMAIL -> {
                String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
                if (!Pattern.matches(emailRegex, valueStr)) {
                    throw new InvalidRequestException("Field '" + label + "' must be a valid email address.");
                }
            }
            default -> {
                // STRING / TEXT / DATE / DATETIME
            }
        }
    }

    private void validateFileConstraints(String fieldName, MultipartFile file) {
        if (file.getSize() > 10 * 1024 * 1024) { // 10 MB limit
            throw new InvalidRequestException("Uploaded file for " + fieldName + " exceeds maximum allowed size (10MB).");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.startsWith("image/") && !contentType.equalsIgnoreCase("application/pdf")) {
            log.warn("File {} has contentType: {}", fieldName, contentType);
        }
    }

    private void validateNomineeMinorGuardianRules(Map<String, Object> submittedFields, Map<String, MultipartFile> uploadedFiles) {
        if (submittedFields == null) return;
        Object minorObj = submittedFields.get("minor");
        if (minorObj == null) return;

        boolean isMinor = Boolean.parseBoolean(minorObj.toString());
        if (isMinor) {
            Object guardianName = submittedFields.get("guardianName");
            Object relation = submittedFields.get("relationshipWithNominee");
            Object guardianNid = submittedFields.get("guardianNidNumber");

            if (guardianName == null || guardianName.toString().isBlank()) {
                throw new InvalidRequestException("Guardian name is required when nominee is a minor.");
            }
            if (relation == null || relation.toString().isBlank()) {
                throw new InvalidRequestException("Relationship with nominee is required when nominee is a minor.");
            }
            if (guardianNid == null || guardianNid.toString().isBlank()) {
                throw new InvalidRequestException("Guardian NID number is required when nominee is a minor.");
            }
        }
    }
}
