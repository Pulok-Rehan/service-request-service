package com.beacepl.service_request_service.entity.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Bank")
public class BankEntity {
    @Id
    private String id;
    private String bankName;
    private String bankShortName;
    private Integer bankCode;
    private String branchName;
    private String branchCode;
    private String routingNumber;
    private String accountNo;
}
