package com.beacepl.service_request_service.repository;

import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ServiceRequestConfigRepository extends MongoRepository<ServiceRequestConfigEntity, String> {
    Optional<ServiceRequestConfigEntity> findByServiceNameAndActiveTrue(String serviceName);
    java.util.List<ServiceRequestConfigEntity> findByActiveTrue();
}
