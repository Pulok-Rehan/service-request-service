package com.beacepl.service_request_service.model;

import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceSectionDto {
    private String section;
    private String sectionDisplayName;
    private List<ServiceRequestConfigEntity> services;
}