package com.beacepl.service_request_service.service.impl;

import com.beacepl.service_request_service.model.AdminActionDto;
import com.beacepl.service_request_service.model.AdminServiceRequestResponseDto;
import com.beacepl.service_request_service.model.ApprovalRequestDto;
import com.beacepl.service_request_service.model.RejectionRequestDto;
import com.beacepl.service_request_service.model.ServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceRequestService {

    private final AdminServiceRequestServiceImpl adminService;

    public ServiceResponse<List<AdminServiceRequestResponseDto>> getPendingRequests() {
        return adminService.listAdminRequests(null, null, null, null, null, 0, 20);
    }

    public ServiceResponse<AdminServiceRequestResponseDto> getRequestById(String id) {
        return adminService.getRequestDetail(id);
    }

    public ServiceResponse<AdminServiceRequestResponseDto> actOnRequest(String id, AdminActionDto dto) {
        if (dto != null && "REJECTED".equalsIgnoreCase(dto.getStatus())) {
            return adminService.rejectRequest(id, RejectionRequestDto.builder()
                    .reviewedBy(dto.getAdminId())
                    .adminRemark(dto.getRemarks())
                    .build());
        }
        return adminService.approveRequest(id, ApprovalRequestDto.builder()
                .reviewedBy(dto != null ? dto.getAdminId() : "ADMIN")
                .adminRemark(dto != null ? dto.getRemarks() : "Approved")
                .build());
    }
}
