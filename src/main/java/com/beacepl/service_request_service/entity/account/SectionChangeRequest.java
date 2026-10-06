package com.beacepl.service_request_service.entity.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SectionChangeRequest {
    private AccountSection section;
    private List<FieldChangeDetail> fields;
    private boolean completed;
}