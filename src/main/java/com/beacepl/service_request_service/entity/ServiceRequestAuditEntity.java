package com.beacepl.service_request_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "ServiceRequestAudit")
public class ServiceRequestAuditEntity {

    @Id
    private String id;

    @Indexed
    private String requestId;

    @Indexed
    private String serviceName;

    @Indexed
    private String accountId;

    private String investorCode;

    private String action;

    private String status;

    private String performedBy;

    private String remark;

    @CreatedDate
    @Indexed
    private LocalDateTime timestamp;
}
