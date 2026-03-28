package com.projects.microservices.core.inventory.service.port.outbound;

import java.time.Instant;

public record LowStockAlertNotificationRequest(
    String productId,
    Integer availableQuantity,
    Integer threshold,
    Instant timestamp,
    String recipient
) {
}
