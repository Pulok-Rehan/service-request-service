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

    private String section;

    private String sectionDisplayName;

    private Integer sectionOrder;

    private Integer displayOrder;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}