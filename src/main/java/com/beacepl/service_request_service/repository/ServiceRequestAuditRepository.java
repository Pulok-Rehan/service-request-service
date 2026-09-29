package com.beacepl.service_request_service.repository;

import com.beacepl.service_request_service.entity.ServiceRequestAuditEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ServiceRequestAuditRepository extends MongoRepository<ServiceRequestAuditEntity, String> {
    List<ServiceRequestAuditEntity> findByRequestId(String requestId);
    List<ServiceRequestAuditEntity> findByAccountId(String accountId);
}
