package com.projects.microservices.core.order.service.port.outbound;

import com.projects.api.core.notification.Notification;
import reactor.core.publisher.Mono;

public interface NotificationPort {
    Mono<Notification> sendOrderConfirmation(Notification notification);
}
