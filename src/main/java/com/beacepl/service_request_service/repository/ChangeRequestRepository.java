package com.beacepl.service_request_service.repository;//package com.beacepl.service_request_service.repository;
//
//import com.beacepl.service_request_service.entity.ChangeRequestEntity;
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface ChangeRequestRepository extends MongoRepository<ChangeRequestEntity, String> {
//    List<ChangeRequestEntity> findByStatus(String status);
//    List<ChangeRequestEntity> findByMobileNumberOrderByRequestedAtDesc(String mobileNumber);
//}


import com.beacepl.service_request_service.entity.ChangeRequestEntity;
import com.beacepl.service_request_service.enums.RequestStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChangeRequestRepository extends MongoRepository<ChangeRequestEntity, String> {
    List<ChangeRequestEntity> findByStatus(RequestStatus status);
    List<ChangeRequestEntity> findByMobileNumberOrderByCreatedAtDesc(String mobileNumber);
    List<ChangeRequestEntity> findByServiceNameAndMobileNumberOrderByCreatedAtDesc(String serviceName, String mobileNumber);
}