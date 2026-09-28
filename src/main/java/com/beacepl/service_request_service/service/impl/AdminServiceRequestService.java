package com.beacepl.service_request_service.service.impl;

import com.beacepl.service_request_service.client.AccountServiceClient;
import com.beacepl.service_request_service.entity.ChangeRequestEntity;
import com.beacepl.service_request_service.entity.FieldConfigEntity;
import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.enums.RequestAction;
import com.beacepl.service_request_service.enums.RequestStatus;
import com.beacepl.service_request_service.model.*;
import com.beacepl.service_request_service.repository.ChangeRequestRepository;
import com.beacepl.service_request_service.repository.ServiceRequestConfigRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceRequestService {

    private final ChangeRequestRepository changeRequestRepository;
    private final ServiceRequestConfigRepository configRepository;
    private final AccountServiceClient accountServiceClient;
    private final ObjectMapper objectMapper;

    public ServiceResponse getPendingRequests() throws JsonProcessingException {
        List<AdminChangeRequestDto> adminChangeRequestDtos = new ArrayList<>();
        List<ChangeRequestEntity> pending = changeRequestRepository.findByStatus(RequestStatus.PENDING);
        for (ChangeRequestEntity entity : pending){
            AdminChangeRequestDto adminChangeRequestDto = mapToAdminDto(entity);
            adminChangeRequestDtos.add(adminChangeRequestDto);
        }
        return new ServiceResponse(false, "Pending service requests fetched", objectMapper.writeValueAsString(adminChangeRequestDtos), "200");
    }

    public ServiceResponse getRequestById(String id) {
        ChangeRequestEntity entity = changeRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Change request not found: " + id));
        return new ServiceResponse(false, "Service request fetched", entity, "200");
    }

    public ServiceResponse actOnRequest(String id, AdminActionDto dto) {
        ChangeRequestEntity entity = changeRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Change request not found: " + id));

        if (entity.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request " + id + " has already been " + entity.getStatus());
        }

        boolean approve = dto.getStatus() != null && dto.getStatus().equalsIgnoreCase("APPROVED");

        if (approve) {
            ServiceRequestConfigEntity config = configRepository.findByServiceNameAndActiveTrue(entity.getServiceName())
                    .orElseThrow(() -> new IllegalStateException(
                            "Cannot approve: service config \"" + entity.getServiceName() + "\" no longer exists/active"));

            AccountPatchRequestDto patchRequest = buildPatchRequest(entity, config);
            accountServiceClient.applyChanges(patchRequest);

            entity.setStatus(RequestStatus.APPROVED);
        } else {
            entity.setStatus(RequestStatus.REJECTED);
        }

        entity.setAdminRemarks(dto.getRemarks());
        entity.setReviewedBy(dto.getAdminId());
        entity.setCompletedAt(LocalDateTime.now());
        changeRequestRepository.save(entity);

        return new ServiceResponse(false, "Request " + entity.getStatus().name().toLowerCase() + " successfully", entity, "200");
    }

    /**
     * Translates a generic ChangeRequestEntity into the generic
     * AccountPatchRequestDto the account service understands, using the
     * accountFieldPath mapping stored on each FieldConfigEntity. No
     * per-service-type code lives here - a brand new service type works
     * automatically as long as its configs accountFieldPath values are
     * correct.
     */
    private AccountPatchRequestDto buildPatchRequest(ChangeRequestEntity entity, ServiceRequestConfigEntity config) {
        Map<String, FieldConfigEntity> fieldsByName = new HashMap<>();
        for (FieldConfigEntity f : config.getFields()) {
            fieldsByName.put(f.getFieldName(), f);
        }

        if (config.isListBased()) {
            AccountFieldUpdateDto.AccountFieldUpdateDtoBuilder listUpdate = AccountFieldUpdateDto.builder()
                    .listFieldName(config.getTargetListField())
                    .identifierField(config.getListIdentifierField());

            if (entity.getAction() == RequestAction.REMOVE) {
                listUpdate.action("REMOVE").identifierValue(entity.getListItemIdentifierValue());
            } else {
                Map<String, Object> values = new HashMap<>();
                if (entity.getFieldValues() != null) {
                    entity.getFieldValues().forEach((fieldName, value) -> {
                        FieldConfigEntity field = fieldsByName.get(fieldName);
                        String path = field != null && field.getAccountFieldPath() != null
                                ? field.getAccountFieldPath()
                                : fieldName;
                        values.put(path, value);
                    });
                }
                listUpdate.action(entity.getAction() == RequestAction.ADD ? "ADD" : "EDIT")
                        .identifierValue(entity.getListItemIdentifierValue())
                        .values(values);
            }

            return AccountPatchRequestDto.builder()
                    .accountId(entity.getAccountId())
                    .listUpdates(List.of(listUpdate.build()))
                    .sourceChangeRequestId(entity.getId())
                    .build();
        }

        Map<String, Object> fieldUpdates = new HashMap<>();
        if (entity.getFieldValues() != null) {
            entity.getFieldValues().forEach((fieldName, value) -> {
                FieldConfigEntity field = fieldsByName.get(fieldName);
                String path = field != null && field.getAccountFieldPath() != null
                        ? field.getAccountFieldPath()
                        : fieldName;
                fieldUpdates.put(path, value);
            });
        }

        return AccountPatchRequestDto.builder()
                .accountId(entity.getAccountId())
                .fieldUpdates(fieldUpdates)
                .sourceChangeRequestId(entity.getId())
                .build();
    }

    private AdminChangeRequestDto mapToAdminDto(ChangeRequestEntity entity) {
        Map<String, Object> fieldValues = entity.getFieldValues();

        boolean containsMinioUrl = fieldValues != null &&
                fieldValues.values().stream()
                        .filter(value -> value instanceof String)
                        .map(Object::toString)
                        .anyMatch(value -> value.contains("http://10.7.93.19"));

        return AdminChangeRequestDto.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .mobileNumber(entity.getMobileNumber())
                .investorCode(entity.getInvestorCode())
                .serviceName(entity.getServiceName())
                .displayName(getServiceDisplayName(entity.getServiceName()))
                .action(entity.getAction())
                .fieldValues(entity.getFieldValues())
                .oldValues(entity.getOldValues())
                .fieldTypes(entity.getFieldTypes())
                .listItemIdentifierValue(entity.getListItemIdentifierValue())
                .status(entity.getStatus())
                .adminRemarks(entity.getAdminRemarks())
                .reviewedBy(entity.getReviewedBy())
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    private String getServiceDisplayName(String serviceName) {

        if (serviceName == null) {
            return "";
        }

        return switch (serviceName.toUpperCase()) {
            case "MOBILE_CHANGE" -> "Mobile Number Change";
            case "EMAIL_CHANGE" -> "Email Address Change";
            case "BANK_CHANGE" -> "Change Bank Account";
            case "TIN_CHANGE" -> "TIN Change";
            case "NOMINEE_ADD" -> "Add Nominee";
            case "NOMINEE_EDIT" -> "Edit Nominee";
            default -> serviceName;
        };
    }
}
