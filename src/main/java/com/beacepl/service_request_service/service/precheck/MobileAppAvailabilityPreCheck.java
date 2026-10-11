package com.beacepl.service_request_service.service.precheck;

import com.beacepl.service_request_service.client.SpClient;
import com.beacepl.service_request_service.entity.ServiceRequestConfigEntity;
import com.beacepl.service_request_service.exceptions.InvalidRequestException;
import com.beacepl.service_request_service.exceptions.MobileAppAlreadyAvailableException;
import com.beacepl.service_request_service.model.ServiceRequestSubmitDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Blocks MOBILE_APPS_REQUEST submission when the SP reports the app is already available for the client.
 */
@Component
@RequiredArgsConstructor
public class MobileAppAvailabilityPreCheck implements ServiceRequestPreCheck {

    public static final String SERVICE_NAME = "MOBILE_APPS_REQUEST";

    private final SpClient spClient;

    @Override
    public boolean supports(String serviceName) {
        return SERVICE_NAME.equalsIgnoreCase(serviceName);
    }

    @Override
    public void check(ServiceRequestSubmitDto submitDto, ServiceRequestConfigEntity config) {
        Map<String, Object> fields = submitDto.getFieldValues();
        String investorCode = firstNonBlank(value(fields, "investorCode"), submitDto.getInvestorCode());
        String platformId = firstNonBlank(value(fields, "platformId"), submitDto.getPlatformId());

        if (investorCode == null || platformId == null) {
            throw new InvalidRequestException("investorCode and platformId are required for Mobile Apps request.");
        }

        if (spClient.isMobileAppAvailable(investorCode, platformId)) {
            throw new MobileAppAlreadyAvailableException(
                    "Mobile app is already available for client " + investorCode + ". Request not submitted.");
        }
    }

    private static String value(Map<String, Object> fields, String key) {
        if (fields == null || fields.get(key) == null) return null;
        return fields.get(key).toString();
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}
