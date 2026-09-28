package com.beacepl.service_request_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "services")
public class ServiceCategory {
    @Id
    private String id;
    private String name;
    private String description;
    private String icon;
    private Integer order;
    private Boolean active;
    private List<ServiceRequestDefinition> serviceRequestList;
}
