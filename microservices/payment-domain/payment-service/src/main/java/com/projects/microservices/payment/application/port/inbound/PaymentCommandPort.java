package com.projects.microservices.payment.application.port.inbound;

import com.projects.api.core.payment.Payment;
import reactor.core.publisher.Mono;

public interface PaymentCommandPort {
    Mono<Payment> processPayment(String orderId, Double amount, String currency, String paymentMethod);
    Mono<Payment> refundPayment(String paymentId, Double amount);
    Mono<Payment> updatePaymentStatus(String paymentId, String status);
}
