package com.projects.microservices.core.notification.persistence;

import com.projects.api.core.notification.NotificationChannel;
import com.projects.api.core.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("notifications")
public class NotificationEntity {
    @Id
    private Long id;
    private String notificationId;
    private String userId;
    private NotificationType type;
    private NotificationChannel channel;
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
