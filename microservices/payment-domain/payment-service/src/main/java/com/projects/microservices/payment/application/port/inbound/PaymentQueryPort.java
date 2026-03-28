package com.projects.microservices.payment.application.port.inbound;

import com.projects.api.core.payment.Payment;
import reactor.core.publisher.Mono;

public interface PaymentQueryPort {
    Mono<Payment> getPayment(String paymentId);
    Mono<Payment> getPaymentByOrderId(String orderId);
}
