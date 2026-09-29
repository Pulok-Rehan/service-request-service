package com.beacepl.service_request_service.repository;

import com.beacepl.service_request_service.entity.ServiceRequestEntity;
import com.beacepl.service_request_service.enums.ServiceRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ServiceRequestRepository extends MongoRepository<ServiceRequestEntity, String> {

    List<ServiceRequestEntity> findByAccountIdAndStatus(String accountId, ServiceRequestStatus status);

    List<ServiceRequestEntity> findByAccountIdAndServiceNameAndStatus(String accountId, String serviceName, ServiceRequestStatus status);

    boolean existsByAccountIdAndServiceNameAndStatus(String accountId, String serviceName, ServiceRequestStatus status);

    Page<ServiceRequestEntity> findByAccountId(String accountId, Pageable pageable);

    Page<ServiceRequestEntity> findByAccountIdAndStatus(String accountId, ServiceRequestStatus status, Pageable pageable);

    Page<ServiceRequestEntity> findByMobileNumberOrEmailOrInvestorCodeOrAccountId(
            String mobileNumber, String email, String investorCode, String accountId, Pageable pageable
    );

    @Query("{ " +
            "'status': ?0, " +
            "'$or': [ {'?1': null}, {'serviceName': ?1} ], " +
            "'$or': [ {'?2': null}, {'investorCode': ?2} ], " +
            "'$or': [ {'?3': null}, {'accountId': ?3} ], " +
            "'$or': [ {'?4': null}, {'mobileNumber': ?4} ] " +
            "}")
    Page<ServiceRequestEntity> findAdminRequests(
            ServiceRequestStatus status,
            String serviceName,
            String investorCode,
            String accountId,
            String mobileNumber,
            Pageable pageable
    );
}
