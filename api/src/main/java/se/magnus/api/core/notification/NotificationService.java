package se.magnus.api.core.notification;

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

    @PatchMapping("/v1/notifications/{notificationId}/read")
    Mono<Notification> markAsRead(String notificationId);

    @DeleteMapping("/v1/notifications/{notificationId}")
    Mono<Void> deleteNotification(String notificationId);
}
