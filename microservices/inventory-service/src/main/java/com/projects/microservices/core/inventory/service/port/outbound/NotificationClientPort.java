package com.projects.microservices.core.inventory.service.port.outbound;

import reactor.core.publisher.Mono;

public interface NotificationClientPort {

    Mono<Void> sendLowStockAlert(LowStockAlertNotificationRequest request);
}
