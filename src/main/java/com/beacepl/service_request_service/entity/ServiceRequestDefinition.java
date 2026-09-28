package com.beacepl.service_request_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestDefinition {
    private String id;
    private String name;
    private String description;
    private Integer order;
    private Boolean active;
    private String apiEndpoint;
    private String action; // e.g., "ADD", "UPDATE"
    private List<FormField> formFields;
    private RequestBodyConfig body;
}
