package com.beacepl.service_request_service.service.impl;

import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.exceptions.ConfigurationNotFoundException;
import com.beacepl.service_request_service.model.ServiceResponse;
import com.beacepl.service_request_service.model.ServiceSectionDto;
import com.beacepl.service_request_service.repository.ServiceRequestConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceRequestConfigService {

    private final ServiceRequestConfigRepository configRepository;

    /**
     * Retrieves all active service request configurations grouped by section for frontend dynamic UI.
     */
    public ServiceResponse<List<ServiceSectionDto>> getActiveConfigurationsGrouped() {
        List<ServiceRequestConfigEntity> activeConfigs = configRepository.findByActiveTrueOrderBySectionOrderAscDisplayOrderAsc();

        Map<String, ServiceSectionDto> sectionMap = new LinkedHashMap<>();

        for (ServiceRequestConfigEntity config : activeConfigs) {
            String sectionKey = config.getSection() != null ? config.getSection() : "GENERAL";
            String sectionName = config.getSectionDisplayName() != null ? config.getSectionDisplayName() : "General Services";
            Integer sectionOrder = config.getSectionOrder() != null ? config.getSectionOrder() : 99;

            sectionMap.computeIfAbsent(sectionKey, k -> ServiceSectionDto.builder()
                    .section(sectionKey)
                    .sectionDisplayName(sectionName)
                    .sectionOrder(sectionOrder)
                    .services(new ArrayList<>())
                    .build()).getServices().add(config);
        }

        List<ServiceSectionDto> sections = new ArrayList<>(sectionMap.values());
        return ServiceResponse.success("Service request configurations retrieved successfully", sections);
    }

    public ServiceResponse<List<ServiceRequestConfigEntity>> listAll() {
        return ServiceResponse.success(configRepository.findAll());
    }

    public ServiceResponse<ServiceRequestConfigEntity> getByName(String serviceName) {
        ServiceRequestConfigEntity config = configRepository.findByServiceName(serviceName)
                .orElseThrow(() -> new ConfigurationNotFoundException("Configuration not found for service: " + serviceName));
        return ServiceResponse.success(config);
    }

    public ServiceResponse<ServiceRequestConfigEntity> create(ServiceRequestConfigEntity config) {
        if (config.getServiceName() == null || config.getServiceName().isBlank()) {
            throw new IllegalArgumentException("serviceName cannot be empty");
        }
        config.setActive(true);
        ServiceRequestConfigEntity saved = configRepository.save(config);
        log.info("Created new ServiceRequestConfigEntity: {}", saved.getServiceName());
        return ServiceResponse.success("Service request configuration created", saved);
    }

    public ServiceResponse<ServiceRequestConfigEntity> update(String id, ServiceRequestConfigEntity config) {
        ServiceRequestConfigEntity existing = configRepository.findById(id)
                .orElseThrow(() -> new ConfigurationNotFoundException("Configuration not found with ID: " + id));

        existing.setDisplayName(config.getDisplayName());
        existing.setDescription(config.getDescription());
        existing.setActive(config.isActive());
        existing.setMultipart(config.isMultipart());
        existing.setListBased(config.isListBased());
        existing.setTargetListField(config.getTargetListField());
        existing.setListIdentifierField(config.getListIdentifierField());
        existing.setAllowedActions(config.getAllowedActions());
        existing.setFields(config.getFields());
        existing.setSection(config.getSection());
        existing.setSectionDisplayName(config.getSectionDisplayName());
        existing.setSectionOrder(config.getSectionOrder());
        existing.setDisplayOrder(config.getDisplayOrder());

        ServiceRequestConfigEntity saved = configRepository.save(existing);
        log.info("Updated ServiceRequestConfigEntity ID: {}", id);
        return ServiceResponse.success("Configuration updated successfully", saved);
    }

    public ServiceResponse<String> deactivate(String id) {
        ServiceRequestConfigEntity config = configRepository.findById(id)
                .orElseThrow(() -> new ConfigurationNotFoundException("Configuration not found with ID: " + id));
        config.setActive(false);
        configRepository.save(config);
        log.info("Deactivated ServiceRequestConfigEntity ID: {}", id);
        return ServiceResponse.success("Configuration deactivated successfully", id);
    }
}
