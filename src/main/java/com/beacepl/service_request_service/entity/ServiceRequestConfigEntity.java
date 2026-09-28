package com.beacepl.service_request_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "ServiceRequestConfig")
public class ServiceRequestConfigEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String serviceName;

    private String displayName;

    private String description;

    private boolean active;

    private boolean multipart;

    private boolean listBased;

    private String targetListField;

    private String listIdentifierField;

    private List<String> allowedActions;

    private List<FieldConfigEntity> fields;

    // ---- NEW: grouping for the client menu ----

    /** Stable key for the group this service belongs to, e.g. "PERSONAL_DETAILS" */
    private String section;

    /** Human-readable label for the group, e.g. "Personal Details" */
    private String sectionDisplayName;

    /** Sort order of the section itself relative to other sections (lower first) */
    private Integer sectionOrder;

    /** Sort order of this service within its section (lower first) */
    private Integer displayOrder;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}