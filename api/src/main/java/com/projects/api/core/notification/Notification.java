package com.projects.api.core.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private String notificationId;
    private String userId;
    private String type; // EMAIL, SMS, PUSH
    private String channel; // ORDER_CONFIRMATION, PAYMENT_SUCCESS, SHIPPING_UPDATE
    private String subject;
    private String message;
    private String recipient;
    private Boolean isRead;
    private java.time.LocalDateTime sentAt;
    private java.time.LocalDateTime readAt;
    private String status; // PENDING, SENT, DELIVERED, FAILED
    private String errorReason;
    private String createdAt;
}
