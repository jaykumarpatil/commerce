package se.magnus.microservices.payment.application.port.inbound;

import se.magnus.api.core.payment.Payment;
import reactor.core.publisher.Mono;

public interface PaymentQueryPort {
    Mono<Payment> getPayment(String paymentId);
    Mono<Payment> getPaymentByOrderId(String orderId);
}
