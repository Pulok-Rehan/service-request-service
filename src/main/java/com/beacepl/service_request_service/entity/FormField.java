package com.beacepl.service_request_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormField {
    private String name;
    private String parameterName;
    private String uiType;
    private Boolean required;
    private String action; // e.g., "ADD", "UPDATE"
}
