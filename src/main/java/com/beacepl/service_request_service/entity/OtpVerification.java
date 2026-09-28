package com.beacepl.service_request_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "otp_verifications")
public class OtpVerification {
    @Id
    private String id;
    private String identifier; // Stores email or mobile number
    private String otp;         // Hashed or plain text depending on security needs (plain shown for simple string matching below)
    private String processName; // e.g., "FORGOT_PASSWORD" or "CHANGE_PASSWORD"
    private java.util.Date createdAt;
}