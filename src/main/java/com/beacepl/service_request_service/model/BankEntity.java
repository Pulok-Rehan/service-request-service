package com.beacepl.service_request_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankEntity {
    private String id;
    private String bankName;
    private String bankShortName;
    private Integer bankCode;
    private String branchName;
    private String branchCode;
    private String routingNumber;
    private String accountNo;
}
