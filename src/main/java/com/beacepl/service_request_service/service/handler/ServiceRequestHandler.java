package com.beacepl.service_request_service.service.handler;

import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.model.AccountSnapshot;
import com.beacepl.service_request_service.model.FieldChangeDetail;

import java.util.List;
import java.util.Map;

public interface ServiceRequestHandler {

    boolean supports(ServiceRequestConfigEntity config);

    Map<String, Object> resolveOldValues(
            AccountSnapshot account,
            ServiceRequestConfigEntity config,
            String listItemIdentifierValue,
            Map<String, Object> submittedFields
    );

    Map<String, Object> resolveNewValues(
            Map<String, Object> submittedFields,
            Map<String, String> uploadedFileObjectNames,
            ServiceRequestConfigEntity config
    );

    List<FieldChangeDetail> buildFieldDetails(
            Map<String, Object> oldValues,
            Map<String, Object> newValues,
            ServiceRequestConfigEntity config
    );
}
