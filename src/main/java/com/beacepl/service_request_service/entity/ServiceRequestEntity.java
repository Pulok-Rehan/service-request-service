package com.beacepl.service_request_service.entity;

import com.beacepl.service_request_service.enums.ServiceRequestStatus;
import com.beacepl.service_request_service.model.FieldChangeDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "ServiceRequest")
@CompoundIndexes({
        @CompoundIndex(name = "account_service_status_idx", def = "{'accountId': 1, 'serviceName': 1, 'status': 1}"),
        @CompoundIndex(name = "status_createdAt_idx", def = "{'status': 1, 'createdAt': -1}")
})
public class ServiceRequestEntity {

    @Id
    private String id;

    @Indexed
    private String accountId;

    @Indexed
    private String mobileNumber;

    @Indexed
    private String investorCode;

    @Indexed
    private String email;

    @Indexed
    private String serviceName;

    private String displayName;

    private String section;

    private String action;

    private Map<String, Object> oldValues;

    private Map<String, Object> newValues;

    /** Detailed list of changed fields with display names, data types, file flags, old values, and new values */
    private List<FieldChangeDetail> fieldDetails;

    private String listItemIdentifierValue;

    @Indexed
    @Builder.Default
    private ServiceRequestStatus status = ServiceRequestStatus.PENDING;

    private String adminRemark;

    private String reviewedBy;

    private LocalDateTime reviewedAt;

    @CreatedDate
    @Indexed
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
