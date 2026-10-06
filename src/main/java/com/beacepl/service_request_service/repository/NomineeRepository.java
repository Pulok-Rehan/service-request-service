package com.beacepl.service_request_service.repository;

import com.beacepl.service_request_service.entity.account.NomineeEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NomineeRepository extends MongoRepository<NomineeEntity, String> {
}
