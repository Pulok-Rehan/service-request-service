package com.beacepl.service_request_service.service;

import com.beacepl.service_request_service.entity.ServiceRequestEntity;
import com.beacepl.service_request_service.entity.account.AccountEntity;
import com.beacepl.service_request_service.entity.account.BankEntity;
import com.beacepl.service_request_service.entity.account.NomineeEntity;
import com.beacepl.service_request_service.exceptions.AccountNotFoundException;
import com.beacepl.service_request_service.exceptions.InvalidRequestException;
import com.beacepl.service_request_service.model.AccountSnapshot;
import com.beacepl.service_request_service.repository.AccountRepository;
import com.beacepl.service_request_service.repository.NomineeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountSnapshotService {

    private final AccountRepository accountRepository;
    private final NomineeRepository nomineeRepository;
    private final ObjectMapper objectMapper;

    public AccountSnapshot getAccountById(String id) {
        if (id == null || id.isBlank()) {
            throw new AccountNotFoundException("Account ID cannot be empty");
        }

        AccountEntity accountEntity = accountRepository.findById(id)
                .orElseGet(() -> accountRepository.findByInvestorCode(id)
                        .orElseGet(() -> accountRepository.findByMobileNumber(id)
                                .orElseGet(() -> accountRepository.findByEmailAddress(id)
                                        .orElseGet(() -> accountRepository.findByAccountNo(id)
                                                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + id))))));

        return convertToSnapshot(accountEntity);
    }

    public AccountSnapshot convertToSnapshot(AccountEntity entity) {
        if (entity == null) return null;
        try {
            return objectMapper.convertValue(entity, AccountSnapshot.class);
        } catch (Exception e) {
            log.error("Failed to convert AccountEntity to AccountSnapshot: {}", e.getMessage(), e);
            throw new RuntimeException("Account data mapping failure", e);
        }
    }

    public void applyServiceRequest(ServiceRequestEntity requestEntity) {
        String accountId = requestEntity.getAccountId();
        AccountEntity account = accountRepository.findById(accountId)
                .orElseGet(() -> accountRepository.findByInvestorCode(accountId)
                        .orElseThrow(() -> new AccountNotFoundException("Cannot apply request. Account not found with ID: " + accountId)));

        Map<String, Object> newValues = requestEntity.getNewValues();
        if (newValues == null || newValues.isEmpty()) {
            log.warn("No new values to apply for service request ID: {}", requestEntity.getId());
            return;
        }

        String serviceName = requestEntity.getServiceName();
        String action = requestEntity.getAction();
        String listItemIdentifier = requestEntity.getListItemIdentifierValue();

        log.info("Applying service request changes locally for service: {}, action: {}, accountId: {}",
                serviceName, action, accountId);

        if ("NOMINEE_ADD".equalsIgnoreCase(serviceName) || ("ADD".equalsIgnoreCase(action) && "nominees".equalsIgnoreCase(serviceName))) {
            applyNomineeAdd(account, newValues);
        } else if ("NOMINEE_EDIT".equalsIgnoreCase(serviceName) || ("EDIT".equalsIgnoreCase(action) && listItemIdentifier != null)) {
            applyNomineeEdit(account, listItemIdentifier, newValues);
        } else {
            applyFieldUpdates(account, newValues);
        }

        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        log.info("Successfully updated AccountEntity for accountId: {}", accountId);
    }

    private void applyNomineeAdd(AccountEntity account, Map<String, Object> newValues) {
        NomineeEntity newNominee = objectMapper.convertValue(newValues, NomineeEntity.class);

        if (newNominee.getPercentage() == 0 && newValues.containsKey("percentage")) {
            try {
                newNominee.setPercentage(Double.parseDouble(newValues.get("percentage").toString()));
            } catch (Exception ignored) {}
        }

        NomineeEntity savedNominee = nomineeRepository.save(newNominee);

        if (account.getNominees() == null) {
            account.setNominees(new ArrayList<>());
        }
        account.getNominees().add(savedNominee);
        log.info("Added new nominee ID: {} to account ID: {}", savedNominee.getId(), account.getId());
    }

    private void applyNomineeEdit(AccountEntity account, String identifierValue, Map<String, Object> newValues) {
        if (account.getNominees() == null || account.getNominees().isEmpty()) {
            throw new InvalidRequestException("Account has no nominees to edit");
        }

        NomineeEntity targetNominee = null;
        for (NomineeEntity nominee : account.getNominees()) {
            if (Objects.equals(nominee.getNid(), identifierValue)
                    || Objects.equals(nominee.getId(), identifierValue)
                    || Objects.equals(nominee.getName(), identifierValue)) {
                targetNominee = nominee;
                break;
            }
        }

        if (targetNominee == null) {
            throw new InvalidRequestException("Nominee not found on account with identifier: " + identifierValue);
        }

        Map<String, Object> existingMap = objectMapper.convertValue(targetNominee, new TypeReference<Map<String, Object>>() {});
        existingMap.putAll(newValues);
        NomineeEntity updatedNominee = objectMapper.convertValue(existingMap, NomineeEntity.class);
        updatedNominee.setId(targetNominee.getId());

        NomineeEntity savedNominee = nomineeRepository.save(updatedNominee);

        List<NomineeEntity> nominees = account.getNominees();
        for (int i = 0; i < nominees.size(); i++) {
            if (Objects.equals(nominees.get(i).getId(), savedNominee.getId())) {
                nominees.set(i, savedNominee);
                break;
            }
        }
        log.info("Updated nominee ID: {} on account ID: {}", savedNominee.getId(), account.getId());
    }

    private void applyFieldUpdates(AccountEntity account, Map<String, Object> newValues) {
        for (Map.Entry<String, Object> entry : newValues.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            switch (key) {
                case "mobileNumber":
                    if (value != null) account.setMobileNumber(value.toString());
                    break;
                case "emailAddress":
                case "email":
                    if (value != null) account.setEmailAddress(value.toString());
                    break;
                case "addressLine1":
                    if (value != null) account.setAddressLine1(value.toString());
                    break;
                case "addressLine2":
                    if (value != null) account.setAddressLine2(value.toString());
                    break;
                case "city":
                    if (value != null) account.setCity(value.toString());
                    break;
                case "state":
                    if (value != null) account.setState(value.toString());
                    break;
                case "zipCode":
                    if (value != null) account.setZipCode(value.toString());
                    break;
                case "country":
                    if (value != null) account.setCountry(value.toString());
                    break;
                case "tinNumber":
                    if (value != null) account.setTinNumber(value.toString());
                    break;
                case "tinCertificate":
                    if (value != null) account.setTinCertificate(value.toString());
                    break;
                case "bank.accountNo":
                case "accountNo":
                    if (account.getBank() == null) account.setBank(new BankEntity());
                    if (value != null) account.getBank().setAccountNo(value.toString());
                    break;
                case "bank.bankName":
                case "bankName":
                    if (account.getBank() == null) account.setBank(new BankEntity());
                    if (value != null) account.getBank().setBankName(value.toString());
                    break;
                case "bank.branchName":
                case "branchName":
                    if (account.getBank() == null) account.setBank(new BankEntity());
                    if (value != null) account.getBank().setBranchName(value.toString());
                    break;
                case "bank.routingNumber":
                case "routingNumber":
                    if (account.getBank() == null) account.setBank(new BankEntity());
                    if (value != null) account.getBank().setRoutingNumber(value.toString());
                    break;
                default:
                    log.debug("Setting field {} -> {}", key, value);
                    break;
            }
        }
    }
}
