package com.beacepl.service_request_service.model;

import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceSectionDto {
    private String section;
    private String sectionDisplayName;
    private Integer sectionOrder;
    private List<ServiceRequestConfigEntity> services;
}