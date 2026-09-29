package com.beacepl.service_request_service.repository;

import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceRequestConfigRepository extends MongoRepository<ServiceRequestConfigEntity, String> {
    Optional<ServiceRequestConfigEntity> findByServiceName(String serviceName);
    Optional<ServiceRequestConfigEntity> findByServiceNameAndActiveTrue(String serviceName);
    List<ServiceRequestConfigEntity> findByActiveTrue();
    List<ServiceRequestConfigEntity> findByActiveTrueOrderBySectionOrderAscDisplayOrderAsc();
}
