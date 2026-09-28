package com.beacepl.service_request_service;

import com.beacepl.service_request_service.model.ServiceResponse;

public class ResponseMessageUtil {

    private ResponseMessageUtil() {
        // Utility class
    }

    public static ServiceResponse otpRequiredResponse(
            String mobileNumber,
            String email) {

        String maskedMobile = maskMobile(mobileNumber);
        String maskedEmail = maskEmail(email);

        String message = String.format(
                "OTP has been sent to mobile %s and email %s",
                maskedMobile,
                maskedEmail
        );

        return new ServiceResponse(message, "427");
    }

    public static String maskMobile(String mobile) {

        if (mobile == null || mobile.length() <= 7) {
            return "****";
        }

        return mobile.substring(0, 3)
                + "****"
                + mobile.substring(mobile.length() - 4);
    }

    public static String maskEmail(String email) {

        if (email == null || !email.contains("@")) {
            return "****";
        }

        String[] parts = email.split("@", 2);
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 2) {
            return "****@" + domain;
        }

        return username.substring(0, 2)
                + "****@"
                + domain;
    }
}