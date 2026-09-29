package com.beacepl.service_request_service.controller;

import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.model.ServiceResponse;
import com.beacepl.service_request_service.model.ServiceSectionDto;
import com.beacepl.service_request_service.service.impl.ServiceRequestConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ServiceRequestConfigController {

    private final ServiceRequestConfigService configService;

    /**
     * Frontend API to retrieve available service request configurations, grouped by section/category.
     * GET /service-request/config
     */
    @GetMapping("/service-request/config")
    public ServiceResponse<List<ServiceSectionDto>> getAvailableConfigurationsGrouped() {
        log.info("Received request for active service configurations grouped by section");
        return configService.getActiveConfigurationsGrouped();
    }

    /**
     * Admin endpoint to list all service configurations.
     */
    @GetMapping("/admin/service-request-config")
    public ServiceResponse<List<ServiceRequestConfigEntity>> listAllConfigs() {
        return configService.listAll();
    }

    /**
     * Admin endpoint to get a single configuration by serviceName.
     */
    @GetMapping("/admin/service-request-config/{serviceName}")
    public ServiceResponse<ServiceRequestConfigEntity> getConfigByName(@PathVariable String serviceName) {
        return configService.getByName(serviceName);
    }

    /**
     * Admin endpoint to create a new service configuration.
     */
    @PostMapping("/admin/service-request-config")
    public ServiceResponse<ServiceRequestConfigEntity> createConfig(@RequestBody ServiceRequestConfigEntity config) {
        return configService.create(config);
    }

    /**
     * Admin endpoint to update an existing configuration.
     */
    @PutMapping("/admin/service-request-config/{id}")
    public ServiceResponse<ServiceRequestConfigEntity> updateConfig(@PathVariable String id, @RequestBody ServiceRequestConfigEntity config) {
        return configService.update(id, config);
    }

    /**
     * Admin endpoint to deactivate a configuration.
     */
    @DeleteMapping("/admin/service-request-config/{id}")
    public ServiceResponse<String> deactivateConfig(@PathVariable String id) {
        return configService.deactivate(id);
    }
}
