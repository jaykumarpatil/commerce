package se.magnus.microservices.payment.domain.event;

import java.time.Instant;

public sealed interface PaymentEvent permits
    PaymentEvent.PaymentInitiated,
    PaymentEvent.PaymentCompleted,
    PaymentEvent.PaymentFailed,
    PaymentEvent.RefundInitiated,
    PaymentEvent.RefundCompleted {

    record PaymentInitiated(
        String paymentId,
        String orderId,
        Double amount,
        String currency,
        Instant timestamp
    ) implements PaymentEvent {}

    record PaymentCompleted(
        String paymentId,
        String orderId,
        String transactionId,
        Double amount,
        Instant timestamp
    ) implements PaymentEvent {}

    record PaymentFailed(
        String paymentId,
        String orderId,
        String reason,
        Instant timestamp
    ) implements PaymentEvent {}

    record RefundInitiated(
        String paymentId,
        String orderId,
        Double amount,
        Instant timestamp
    ) implements PaymentEvent {}

    record RefundCompleted(
        String paymentId,
        String orderId,
        String refundId,
        Instant timestamp
    ) implements PaymentEvent {}
}
