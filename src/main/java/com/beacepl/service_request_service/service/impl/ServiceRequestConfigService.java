package com.beacepl.service_request_service.service.impl;

import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.model.ServiceResponse;
import com.beacepl.service_request_service.repository.ServiceRequestConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

/**
 * CRUD over ServiceRequestConfigEntity, exposed to admins only. This is the
 * mechanism by which a brand new service request type (say, "ADDRESS_CHANGE")
 * gets added to the system with zero code deployment - an admin (or a
 * seeding script) just posts a new config document.
 */
@Service
@RequiredArgsConstructor
public class ServiceRequestConfigService {

    private final ServiceRequestConfigRepository repository;

    public ServiceResponse listAll() {
        return new ServiceResponse(false, "Configs fetched", repository.findAll(), "200");
    }

    public ServiceResponse getByName(String serviceName) {
        ServiceRequestConfigEntity config = repository.findByServiceNameAndActiveTrue(serviceName)
                .orElseThrow(() -> new NoSuchElementException("Unknown or inactive service: " + serviceName));
        return new ServiceResponse(false, "Config fetched", config, "200");
    }

    public ServiceResponse create(ServiceRequestConfigEntity config) {
        if (repository.findByServiceNameAndActiveTrue(config.getServiceName()).isPresent()) {
            throw new IllegalArgumentException("An active config already exists for " + config.getServiceName());
        }
        config.setId(null);
        config.setActive(true);
        return new ServiceResponse(false, "Config created", repository.save(config), "200");
    }

    public ServiceResponse update(String id, ServiceRequestConfigEntity updated) {
        ServiceRequestConfigEntity existing = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Config not found: " + id));
        updated.setId(existing.getId());
        return new ServiceResponse(false, "Config updated", repository.save(updated), "200");
    }

    public ServiceResponse deactivate(String id) {
        ServiceRequestConfigEntity existing = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Config not found: " + id));
        existing.setActive(false);
        return new ServiceResponse(false, "Config deactivated", repository.save(existing), "200");
    }

}
