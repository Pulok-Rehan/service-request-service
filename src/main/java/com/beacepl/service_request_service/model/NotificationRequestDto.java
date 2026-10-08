package com.beacepl.service_request_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {

    private String title;
    private String body;
    private String image;
    private String icon;
    private String clickAction;
    private String deepLink;

    private String notificationType; // SUCCESS, INFO, WARNING, ERROR
    private String priority;         // HIGH, MEDIUM, LOW
    private String channel;          // WEBSOCKET, PUSH, IN_APP, ALL

    private String sender;

    private String receiverPlatformId;        // unicast
    private List<String> receiverPlatformIds; // multicast
    private String topic;                 // topic
    private String role;                  // role

    private Map<String, Object> data;
}
