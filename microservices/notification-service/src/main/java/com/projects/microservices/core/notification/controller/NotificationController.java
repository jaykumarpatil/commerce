package com.projects.microservices.core.notification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.projects.api.core.notification.*;
import com.projects.util.http.ServiceUtil;

@RestController
public class NotificationController implements NotificationService {

    private final NotificationService notificationService;
    private final ServiceUtil serviceUtil;

    @Autowired
    public NotificationController(NotificationService notificationService, ServiceUtil serviceUtil) {
        this.notificationService = notificationService;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Notification> sendNotification(Notification notification) {
        return notificationService.sendNotification(notification);
    }

    @Override
    public Mono<Void> sendEmail(EmailNotification notification) {
        return notificationService.sendEmail(notification);
    }

    @Override
    public Mono<Void> sendSMS(SMSNotification notification) {
        return notificationService.sendSMS(notification);
    }

    @Override
    public Mono<Notification> getNotification(String notificationId) {
        return notificationService.getNotification(notificationId);
    }

    @Override
    public Flux<Notification> getNotificationsByUserId(String userId) {
        return notificationService.getNotificationsByUserId(userId);
    }

    @Override
    public Mono<Notification> markAsRead(String notificationId) {
        return notificationService.markAsRead(notificationId);
    }

    @Override
    public Mono<Void> deleteNotification(String notificationId) {
        return notificationService.deleteNotification(notificationId);
    }
}
