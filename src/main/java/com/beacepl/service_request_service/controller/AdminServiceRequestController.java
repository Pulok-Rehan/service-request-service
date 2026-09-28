//package com.beacepl.service_request_service.controller;
//
//import com.beacepl.service_request_service.client.AccountServiceClient;
//import com.beacepl.service_request_service.entity.ChangeRequestEntity;
//import com.beacepl.service_request_service.model.*;
//import com.beacepl.service_request_service.repository.ChangeRequestRepository;
//import com.beacepl.service_request_service.service.ServiceRequestService;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.crossstore.ChangeSetPersister;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/admin/service-requests")
//@RequiredArgsConstructor
//public class AdminServiceRequestController {
//
//    private final ChangeRequestRepository repository;
//    private final ServiceRequestService service;
//    private final AccountServiceClient accountServiceClient;
//    private final ObjectMapper objectMapper;
//
//    @GetMapping("/pending")
//    public ServiceResponse getPendingRequests() throws JsonProcessingException {
//        List<ChangeRequestEntity> changeRequestEntities = repository.findByStatus("PENDING");
//        if (changeRequestEntities.isEmpty()) {
//            return new ServiceResponse(false, "No Service Request found", objectMapper.writeValueAsString(changeRequestEntities), "200");
//        }
//        return new ServiceResponse(false, "Service request found", objectMapper.writeValueAsString(changeRequestEntities), "200");
//    }
//
//    @PostMapping("/{id}")
//    public ServiceResponse approveRequest(@PathVariable String id,
//                                          @RequestBody AdminActionDto dto) throws ChangeSetPersister.NotFoundException {
//        ChangeRequestEntity entity = repository.findById(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
//
//        if (dto.getStatus() != null && dto.getStatus().equalsIgnoreCase("REJECTED")) {
//            entity.setStatus("REJECTED");
//            entity.setAdminRemarks(dto.getRemarks());
//        } else {
//            // 1. Aggregate all changes from the list of Request objects
//            Map<String, Object> allChanges = new HashMap<>();
//            if (entity.getRequest() != null) {
//                for (Request request : entity.getRequest()) {
//                    if (request.getRequestedValue() != null) {
//                        allChanges.putAll(request.getRequestedValue());
//                    }
//                }
//            }
//
//            // 2. Prepare AcceptChangeDto
//            AcceptChangeDto acceptChangeDto = AcceptChangeDto.builder()
//                    .mobileNumber(entity.getMobileNumber())
//                    .accountId(entity.getAccountId())
//                    .serviceName(entity.getRequestedFor())
//                    .action(entity.getAction())
//                    .fieldValues(allChanges)
//                    .build();
//
//            // 3. Apply changes to AccountEntity via RestTemplate client
//            accountServiceClient.applyChanges(acceptChangeDto);
//
//            entity.setStatus("APPROVED");
//        }
//
//        entity.setAdminRemarks(dto.getRemarks());
//        entity.setCompletedAt(LocalDateTime.now());
//        repository.save(entity);
//
//        return new ServiceResponse(false, "Request status updated successfully", entity, "200");
//    }
//
//    @GetMapping("/pending")
//    public ServiceResponse getPendingRequests() {
//        return adminServiceRequestService.getPendingRequests();
//    }
//
//}

package com.beacepl.service_request_service.controller;

import com.beacepl.service_request_service.model.AdminActionDto;
import com.beacepl.service_request_service.model.ServiceResponse;
import com.beacepl.service_request_service.service.impl.AdminServiceRequestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/service-requests")
@Slf4j
@RequiredArgsConstructor
public class AdminServiceRequestController {

    private final AdminServiceRequestService adminServiceRequestService;

    @GetMapping("/pending")
    public ServiceResponse getPendingRequests() throws JsonProcessingException {
        return adminServiceRequestService.getPendingRequests();
    }

    @GetMapping("/{id}")
    public ServiceResponse getRequest(@PathVariable String id) {
        return adminServiceRequestService.getRequestById(id);
    }

    /**
     * Approve or reject a pending request. On approval, this triggers a
     * call to dbp-onboarding-service to apply the change to AccountEntity.
     * Body: { "status": "APPROVED" | "REJECTED", "remarks": "...", "reviewedBy": "admin@bracepl.com" }
     */
    @PostMapping("/{id}/action")
    public ServiceResponse actOnRequest(@PathVariable String id, @RequestBody AdminActionDto dto) {
        log.info("Admin action on request {}: {}", id, dto.getStatus());
        return adminServiceRequestService.actOnRequest(id, dto);
    }
}
