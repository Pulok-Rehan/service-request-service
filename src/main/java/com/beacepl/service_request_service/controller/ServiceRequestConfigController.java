package com.beacepl.service_request_service.controller;

import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.model.ServiceResponse;
import com.beacepl.service_request_service.service.impl.ServiceRequestConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only management of service request "types". This is how a new
 * request type (e.g. ADDRESS_CHANGE) gets introduced without a code
 * deployment: POST a new ServiceRequestConfigEntity here, describing its
 * fields and how they map onto AccountEntity, and it immediately becomes
 * available at /service-request/submit.
 */
@RestController
@RequestMapping("/admin/service-request-config")
@RequiredArgsConstructor
public class ServiceRequestConfigController {

    private final ServiceRequestConfigService configService;

    @GetMapping
    public ServiceResponse listAll() {
        return configService.listAll();
    }

    @GetMapping("/{serviceName}")
    public ServiceResponse getByName(@PathVariable String serviceName) {
        return configService.getByName(serviceName);
    }

    @PostMapping
    public ServiceResponse create(@RequestBody ServiceRequestConfigEntity config) {
        return configService.create(config);
    }

    @PutMapping("/{id}")
    public ServiceResponse update(@PathVariable String id, @RequestBody ServiceRequestConfigEntity config) {
        return configService.update(id, config);
    }

    @DeleteMapping("/{id}")
    public ServiceResponse deactivate(@PathVariable String id) {
        return configService.deactivate(id);
    }
}
