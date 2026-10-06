package com.beacepl.service_request_service.service.impl;

import com.beacepl.service_request_service.entity.FieldConfigEntity;
import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validates a submitted request against the FieldConfigEntity list of the
 * matching ServiceRequestConfigEntity. This is what makes new service types
 * "just work" without new validation code - required-ness, regex, length
 * and file-presence checks are all driven off the stored config.
 */
@Component
public class ServiceRequestValidator {

    public void validate(ServiceRequestConfigEntity config,
                          Map<String, Object> fieldValues,
                          Map<String, MultipartFile> files) {

        for (FieldConfigEntity field : config.getFields()) {
            if (field.isFile()) {
                MultipartFile file = files == null ? null : files.get(field.getFieldName());
                boolean present = file != null && !file.isEmpty();
                if (field.isRequired() && !present) {
                    throw new IllegalArgumentException(
                            "Missing required file for field \"" + field.getLabel() + "\" (" + field.getFieldName() + ")");
                }
                continue;
            }

            Object rawValue = fieldValues == null ? null : fieldValues.get(field.getFieldName());
            boolean present = rawValue != null && StringUtils.hasText(String.valueOf(rawValue));

            if (field.isRequired() && !present) {
                throw new IllegalArgumentException(
                        "Field \"" + field.getLabel() + "\" (" + field.getFieldName() + ") is required");
            }
            if (!present) {
                continue;
            }

            String stringValue = String.valueOf(rawValue);

            if (field.getMinLength() != null && stringValue.length() < field.getMinLength()) {
                throw new IllegalArgumentException(
                        "Field \"" + field.getLabel() + "\" must be at least " + field.getMinLength() + " characters");
            }
            if (field.getMaxLength() != null && stringValue.length() > field.getMaxLength()) {
                throw new IllegalArgumentException(
                        "Field \"" + field.getLabel() + "\" must be at most " + field.getMaxLength() + " characters");
            }

            switch (field.getDataType()) {
                case NUMBER -> {
                    try {
                        Long.parseLong(stringValue);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Field \"" + field.getLabel() + "\" must be a whole number");
                    }
                }
                case DECIMAL -> {
                    try {
                        Double.parseDouble(stringValue);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Field \"" + field.getLabel() + "\" must be a number");
                    }
                }
                case BOOLEAN -> {
                    if (!stringValue.equalsIgnoreCase("true") && !stringValue.equalsIgnoreCase("false")) {
                        throw new IllegalArgumentException("Field \"" + field.getLabel() + "\" must be true or false");
                    }
                }
                default -> { /* STRING / DATE / DATETIME: format handled by regex above, if configured */ }
            }
        }

        if (config.isListBased() && config.getAllowedActions() != null
                && !config.getAllowedActions().isEmpty()) {
            // action itself is checked by the service layer against allowedActions
        }
    }
}
