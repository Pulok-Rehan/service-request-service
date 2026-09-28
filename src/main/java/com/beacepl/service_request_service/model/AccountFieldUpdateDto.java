package com.beacepl.service_request_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Describes an ADD/EDIT/REMOVE against a list field on AccountEntity
 * (e.g. nominees). Sent as part of AccountPatchRequestDto.listUpdates.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountFieldUpdateDto {

    /** Name of the list field on AccountEntity, e.g. "nominees" */
    private String listFieldName;

    /** ADD, EDIT, or REMOVE */
    private String action;

    /** Field name (within the list item) used to identify an existing entry, e.g. "nid" */
    private String identifierField;

    private String identifierValue;

    /** field -> value for the list item (ignored for REMOVE) */
    private Map<String, Object> values;
}
