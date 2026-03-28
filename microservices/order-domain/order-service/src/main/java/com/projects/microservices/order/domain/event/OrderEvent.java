package com.projects.microservices.order.domain.event;

import java.time.Instant;
import java.util.List;

public sealed interface OrderEvent permits
    OrderEvent.Created,
    OrderEvent.StatusChanged,
    OrderEvent.PaymentReceived,
    OrderEvent.Shipped,
    OrderEvent.Delivered,
    OrderEvent.Cancelled {

    record Created(
        String orderId,
        String userId,
        Double totalAmount,
        Instant timestamp
    ) implements OrderEvent {}

    record StatusChanged(
        String orderId,
        String previousStatus,
        String newStatus,
        Instant timestamp
    ) implements OrderEvent {}

    record PaymentReceived(
        String orderId,
        String paymentId,
        Double amount,
        Instant timestamp
    ) implements OrderEvent {}

    record Shipped(
        String orderId,
        String trackingNumber,
        String carrier,
        Instant timestamp
    ) implements OrderEvent {}

    record Delivered(
        String orderId,
        Instant timestamp
    ) implements OrderEvent {}

    record Cancelled(
        String orderId,
        String reason,
        Instant timestamp
    ) implements OrderEvent {}
}
