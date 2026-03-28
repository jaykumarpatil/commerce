package com.projects.api.core.notification;

import com.projects.api.core.common.PaginationRequest;
import com.projects.api.core.common.PaginationResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationService {

    @PostMapping("/v1/notifications")
    Mono<Notification> sendNotification(Notification notification);

    @PostMapping("/v1/notifications/email")
    Mono<Void> sendEmail(EmailNotification notification);

    @PostMapping("/v1/notifications/sms")
    Mono<Void> sendSMS(SMSNotification notification);

    @GetMapping("/v1/notifications/{notificationId}")
    Mono<Notification> getNotification(String notificationId);

    @GetMapping("/v1/notifications/user/{userId}")
    Flux<Notification> getNotificationsByUserId(String userId);

    default Mono<PaginationResponse<Notification>> getNotificationsByUserId(String userId, PaginationRequest paginationRequest) {
        final PaginationRequest request = paginationRequest == null ? new PaginationRequest() : paginationRequest;
        return getNotificationsByUserId(userId)
            .skip((long) request.getPage() * request.getSize())
            .take(request.getSize())
            .collectList()
            .map(items -> PaginationResponse.of(items, request.getPage(), request.getSize()));
    }

    @PatchMapping("/v1/notifications/{notificationId}/read")
    Mono<Notification> markAsRead(String notificationId);

    Mono<Notification> updateNotificationStatus(String notificationId, NotificationStatus status);

    @Deprecated
    @PatchMapping("/v1/notifications/{notificationId}/status")
    default Mono<Notification> updateNotificationStatus(String notificationId, String status) {
        return updateNotificationStatus(notificationId, NotificationStatus.from(status));
    }

    @DeleteMapping("/v1/notifications/{notificationId}")
    Mono<Void> deleteNotification(String notificationId);
}
