package com.beacepl.service_request_service;


import com.beacepl.service_request_service.entity.ServiceRequestEntity;
import com.beacepl.service_request_service.enums.ServiceRequestStatus;
import com.beacepl.service_request_service.service.impl.NotificationClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ServiceRequestNotificationUtil {

    private static final String CUSTOMER_TITLE = "Service Request Notification";
    private static final String RM_TITLE = "Service Request Notification";
    private static final String MANAGEMENT_TITLE = "Service Request Notification";

    private static final String CUSTOMER_MOBILE = null; // Customer uses platformId
    private static final String RM_MOBILE = "6aba0e378b5a2d487f048974";
    private static final String MANAGEMENT_MOBILE = "6aba49c48b5a2d487f04897f";

    private static final String NOTIFICATION_TYPE = "INFO";
    private static final String CLICK_ACTION = "STATUS_PAGE";

    private final NotificationClientService notificationClientService;

    public void sendNotification(
            ServiceRequestEntity serviceRequestEntity,
            ServiceRequestStatus status) {

        String investorCode = serviceRequestEntity.getInvestorCode();
        String serviceName = serviceRequestEntity.getServiceName();

        String customerMessage;
        String rmMessage;
        String managementMessage = null;

        if (ServiceRequestStatus.PENDING.equals(status)) {

            customerMessage = String.format(
                    "Service request from your BESL A/C %s has been submitted successfully for %s.",
                    investorCode,
                    serviceName
            );

            rmMessage = String.format(
                    "Service request from your client's BESL A/C %s has been submitted successfully for %s.",
                    investorCode,
                    serviceName
            );

            managementMessage = String.format(
                    "Service request from client %s has been submitted successfully for %s.",
                    investorCode,
                    serviceName
            );

        } else {

            customerMessage = String.format(
                    "Service request from your BESL A/C %s has been approved for %s.",
                    investorCode,
                    serviceName
            );

            rmMessage = String.format(
                    "Service request from your client's BESL A/C %s has been approved for %s.",
                    investorCode,
                    serviceName
            );
        }

        Map<String, Object> notificationData =
                buildNotificationData(serviceRequestEntity);

        // Customer notification
        notificationClientService.sendUnicast(
                serviceRequestEntity.getPlatformid(),
                CUSTOMER_TITLE,
                customerMessage,
                NOTIFICATION_TYPE,
                notificationData,
                CLICK_ACTION
        );

        // RM notification
        notificationClientService.sendUnicast(
                RM_MOBILE,
                RM_TITLE,
                rmMessage,
                NOTIFICATION_TYPE,
                notificationData,
                CLICK_ACTION
        );

        // Management notification only for PENDING
        if (managementMessage != null) {
            notificationClientService.sendUnicast(
                    MANAGEMENT_MOBILE,
                    MANAGEMENT_TITLE,
                    managementMessage,
                    NOTIFICATION_TYPE,
                    notificationData,
                    CLICK_ACTION
            );
        }
    }

    private Map<String, Object> buildNotificationData(
            ServiceRequestEntity serviceRequestEntity) {

        return Map.of(
                "investorCode",
                serviceRequestEntity.getInvestorCode(),

                "mobileNumber",
                serviceRequestEntity.getMobileNumber(),

                "clientLink",
                "/service_request_history?type="
                        + serviceRequestEntity.getAction(),
                "employeeLink",
                "/service_request_history?type="
                        + serviceRequestEntity.getAction(),

                "webLink",
                buildWebLink(serviceRequestEntity)
        );
    }

    private String buildWebLink(
            ServiceRequestEntity serviceRequestEntity) {

        return "/service-requests"
                + "?status=" + serviceRequestEntity.getStatus()
                + "&investorCode=" + serviceRequestEntity.getInvestorCode()
                + "&accountId=" + serviceRequestEntity.getAccountId()
                + "&mobileNumber=" + serviceRequestEntity.getMobileNumber()
                + "&serviceName=" + serviceRequestEntity.getServiceName();
    }
}