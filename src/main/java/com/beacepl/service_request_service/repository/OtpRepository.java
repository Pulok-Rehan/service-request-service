package com.beacepl.service_request_service.repository;

import com.beacepl.service_request_service.entity.OtpVerification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OtpRepository extends MongoRepository<OtpVerification, String> {
    List<OtpVerification> findByIdentifierAndProcessNameOrderByCreatedAtDesc(String identifier, String processName);
}
