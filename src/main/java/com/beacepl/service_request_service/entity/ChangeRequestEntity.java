//package com.beacepl.service_request_service.entity;
//
//import com.beacepl.service_request_service.model.Request;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//@Document(collection = "changeRequests")
//public class ChangeRequestEntity {
//
//    @Id
//    private String id;
//
//    private String mobileNumber;
//    private String investorCode;
//    private String requestedBy;
//    private String requestedFor; // e.g., "MOBILE_NUMBER", "EMAIL", "ADDRESS"
//    private String action; // e.g., "ADD", "UPDATE"
//
//    // Dynamic list of requested changes
//    private List<Request> request;
//    private String status;
//    private String accountId;
//    private String email;
//
//    private String adminRemarks;
//    private String userRemarks;
//
//    @CreatedDate
//    private LocalDateTime requestedAt;
//    private LocalDateTime completedAt;
//
//    private boolean userNotified;
//    private LocalDateTime notifiedAt;
//}


package com.beacepl.service_request_service.entity;

import com.beacepl.service_request_service.enums.RequestAction;
import com.beacepl.service_request_service.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * A single submitted, generic change request. Regardless of which
 * ServiceRequestConfigEntity it was submitted against, it is stored in
 * exactly this shape - fieldValues is a flat map of fieldName -> value
 * (file fields already resolved to a stored path/URL by this point).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "ChangeRequest")
public class ChangeRequestEntity {

    @Id
    private String id;

    private String accountId;

    private String mobileNumber;
    private String investorCode;

    /** Matches ServiceRequestConfigEntity.serviceName */
    private String serviceName;

    private RequestAction action;

    /** fieldName -> submitted value (strings, numbers, booleans, or a stored file path for FILE fields) */
    private Map<String, Object> fieldValues;
    Map<String, String> fieldTypes;
    private Map<String, Object> oldValues;

    /**
     * For listBased services (e.g. nominee), the value of the list
     * identifier field used to locate the existing item on EDIT/REMOVE.
     * Null for ADD and for non-list-based services.
     */
    private String listItemIdentifierValue;

    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    private String adminRemarks;

    private String reviewedBy;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;
}
