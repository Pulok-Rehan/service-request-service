package com.beacepl.service_request_service.service.impl;

import com.beacepl.service_request_service.MinioUtil;
import com.beacepl.service_request_service.model.NotificationRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationClientService {

    private final RestTemplate restTemplate;

    // Optional KafkaTemplate if Kafka is enabled in this microservice
//    @Autowired(required = false)
//    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${notification.service.base-url}")
    private String notificationServiceBaseUrl;

    @Value("${service.name}")
    private String serviceName;

    @Value("${service.logo}")
    private String serviceLogo;

    // =========================================================================
    // 1. NOTIFICATION SEND PATH (HTTP -> Notification Controller -> Redis -> WS)
    // =========================================================================

    /**
     * Send notification to a single user (Unicast).
     * Automatically increments Redis unread count & pushes WS message to /user/notifications
     */
    public void sendUnicast(String mobile, String title, String body, String type, Map<String, Object> data, String clickAction) {
        NotificationRequestDto request = NotificationRequestDto.builder()
                .title(title)
                .body(body)
                .receiverMobile(mobile)
                .notificationType(type != null ? type : "INFO")
                .priority("HIGH")
                .channel("CLIENT_PORTAL")
                .sender(serviceName)
                .image(MinioUtil.getImageUrl(serviceLogo))
                .icon(MinioUtil.getImageUrl(serviceLogo))
                .data(data)
                .clickAction(clickAction)
                .build();

        sendHttpRequest("/notifications/unicast", request);
    }

    /**
     * Send notification to multiple specific users (Multicast).
     */
    public void sendMulticast(List<String> mobiles, String title, String body, String type, Map<String, Object> data) {
        NotificationRequestDto request = NotificationRequestDto.builder()
                .title(title)
                .body(body)
                .receiverMobiles(mobiles)
                .notificationType(type != null ? type : "INFO")
                .priority("HIGH")
                .channel("CLIENT_PORTAL")
                .sender(serviceName)
                .image(MinioUtil.getImageUrl(serviceLogo))
                .icon(MinioUtil.getImageUrl(serviceLogo))
                .data(data)
                .build();

        sendHttpRequest("/notifications/multicast", request);
    }

    /**
     * Send broadcast notification to all connected users (/topic/global).
     */
    public void sendBroadcast(String title, String body, String type, Map<String, Object> data) {
        NotificationRequestDto request = NotificationRequestDto.builder()
                .title(title)
                .body(body)
                .notificationType(type != null ? type : "INFO")
                .priority("HIGH")
                .channel("CLIENT_PORTAL")
                .image(MinioUtil.getImageUrl(serviceLogo))
                .icon(MinioUtil.getImageUrl(serviceLogo))
                .sender(serviceName)
                .data(data)
                .build();

        sendHttpRequest("/notifications/broadcast", request);
    }

    /**
     * Send notification to a specific topic feed (e.g. topic = "ipo" or "attendance").
     */
    public void sendToTopic(String topic, String title, String body, String type, Map<String, Object> data) {
        NotificationRequestDto request = NotificationRequestDto.builder()
                .title(title)
                .body(body)
                .topic(topic)
                .notificationType(type != null ? type : "INFO")
                .priority("HIGH")
                .channel("CLIENT_PORTAL")
                .sender(serviceName)
                .image(MinioUtil.getImageUrl(serviceLogo))
                .icon(MinioUtil.getImageUrl(serviceLogo))
                .data(data)
                .build();

        sendHttpRequest("/notifications/topic", request);
    }

    // =========================================================================
    // 2. LIVE DATABASE CHANGE EVENT PATH (Kafka -> DatabaseChangeEventListener)
    // =========================================================================

    /**
     * Publishes a generic domain change event for real-time live data updates.
     * Consumed by DatabaseChangeEventListener in notification-service and pushed
     * to /topic/{module} or /user/{mobile}/updates without page refresh.
     */
//    public void publishDatabaseChangeEvent(String module, String eventType, String entityId, String mobileNumber, Map<String, Object> payload) {
//        DatabaseChangeEventDto event = DatabaseChangeEventDto.builder()
//                .module(module)
//                .eventType(eventType)
//                .entityId(entityId)
//                .mobileNumber(mobileNumber)
//                .payload(payload)
//                .timestamp(Instant.now())
//                .build();
//
//        if (kafkaTemplate != null) {
//            try {
//                String json = objectMapper.writeValueAsString(event);
//                kafkaTemplate.send("notification.events", json);
//                log.info("Published DatabaseChangeEvent to Kafka [notification.events]: module={} type={}", module, eventType);
//            } catch (Exception e) {
//                log.error("Failed to publish DatabaseChangeEvent to Kafka: {}", e.getMessage(), e);
//            }
//        } else {
//            log.warn("KafkaTemplate not configured in {}; fallback to HTTP unicast/topic required.", serviceName);
//        }
//    }

    // Helper method for HTTP REST Execution
    private void sendHttpRequest(String endpoint, Object requestBody) {
        try {
            String url = notificationServiceBaseUrl + endpoint;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
            restTemplate.postForEntity(url, entity, String.class);

            log.info("Successfully sent notification request to {} endpoint {}", serviceName, endpoint);
        } catch (Exception e) {
            log.error("Failed to send notification via HTTP to {}: {}", endpoint, e.getMessage(), e);
        }
    }
}
