package com.beacepl.service_request_service.repository;

import com.beacepl.service_request_service.entity.account.AccountEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends MongoRepository<AccountEntity, String> {

    Optional<AccountEntity> findByInvestorCode(String investorCode);

    Optional<AccountEntity> findByMobileNumber(String mobileNumber);

    Optional<AccountEntity> findByEmailAddress(String emailAddress);

    Optional<AccountEntity> findByBoNumber(String boNumber);

    Optional<AccountEntity> findByAccountNo(String accountNo);
}
