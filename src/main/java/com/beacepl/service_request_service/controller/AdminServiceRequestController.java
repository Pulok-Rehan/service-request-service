package com.beacepl.service_request_service.controller;

import com.beacepl.service_request_service.enums.ServiceRequestStatus;
import com.beacepl.service_request_service.model.AdminActionDto;
import com.beacepl.service_request_service.model.AdminServiceRequestResponseDto;
import com.beacepl.service_request_service.model.ApprovalRequestDto;
import com.beacepl.service_request_service.model.RejectionRequestDto;
import com.beacepl.service_request_service.model.ServiceResponse;
import com.beacepl.service_request_service.service.impl.AdminServiceRequestServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/service-request")
@Slf4j
@RequiredArgsConstructor
public class AdminServiceRequestController {

    private final AdminServiceRequestServiceImpl adminService;

    /**
     * Admin list API with pagination and filters.
     * GET /admin/service-request
     */
    @GetMapping
    public ServiceResponse<List<AdminServiceRequestResponseDto>> listRequests(
            @RequestParam(required = false) ServiceRequestStatus status,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String investorCode,
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String mobileNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Admin list service requests query: status={}, serviceName={}, page={}, size={}", status, serviceName, page, size);
        return adminService.listAdminRequests(status, serviceName, investorCode, accountId, mobileNumber, page, size);
    }

    /**
     * Admin view request detail with presigned file URLs.
     * GET /admin/service-request/{id}
     */
    @GetMapping("/{id}")
    public ServiceResponse<AdminServiceRequestResponseDto> getRequestDetail(@PathVariable String id) {
        log.info("Admin fetching detail for service request ID: {}", id);
        return adminService.getRequestDetail(id);
    }

    /**
     * Admin approve request API.
     * POST /admin/service-request/{id}/approve or PUT /admin/service-request/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public ServiceResponse<AdminServiceRequestResponseDto> approveRequest(
            @PathVariable String id,
            @RequestBody(required = false) ApprovalRequestDto dto
    ) {
        log.info("Admin approving service request ID: {}", id);
        return adminService.approveRequest(id, dto);
    }

    @PutMapping("/{id}/approve")
    public ServiceResponse<AdminServiceRequestResponseDto> approveRequestPut(
            @PathVariable String id,
            @RequestBody(required = false) ApprovalRequestDto dto
    ) {
        return approveRequest(id, dto);
    }

    /**
     * Admin reject request API.
     * POST /admin/service-request/{id}/reject or PUT /admin/service-request/{id}/reject
     */
    @PostMapping("/{id}/reject")
    public ServiceResponse<AdminServiceRequestResponseDto> rejectRequest(
            @PathVariable String id,
            @RequestBody(required = false) RejectionRequestDto dto
    ) {
        log.info("Admin rejecting service request ID: {}", id);
        return adminService.rejectRequest(id, dto);
    }

    @PutMapping("/{id}/reject")
    public ServiceResponse<AdminServiceRequestResponseDto> rejectRequestPut(
            @PathVariable String id,
            @RequestBody(required = false) RejectionRequestDto dto
    ) {
        return rejectRequest(id, dto);
    }

    /**
     * Compatibility action endpoint.
     * POST /admin/service-request/{id}/action
     */
    @PostMapping("/{id}/action")
    public ServiceResponse<AdminServiceRequestResponseDto> actOnRequest(
            @PathVariable String id,
            @RequestBody AdminActionDto dto
    ) {
        if (dto != null && "REJECTED".equalsIgnoreCase(dto.getStatus())) {
            return adminService.rejectRequest(id, RejectionRequestDto.builder()
                    .reviewedBy(dto.getAdminId())
                    .adminRemark(dto.getRemarks())
                    .build());
        } else {
            return adminService.approveRequest(id, ApprovalRequestDto.builder()
                    .reviewedBy(dto != null ? dto.getAdminId() : "ADMIN")
                    .adminRemark(dto != null ? dto.getRemarks() : "Approved")
                    .build());
        }
    }

    /**
     * Admin retry execution API if downstream dispatch failed.
     * POST /admin/service-request/{id}/retry-execution
     */
    @PostMapping("/{id}/retry-execution")
    public ServiceResponse<AdminServiceRequestResponseDto> retryExecution(@PathVariable String id) {
        log.info("Admin retrying downstream execution for service request ID: {}", id);
        return adminService.retryExecution(id);
    }
}
