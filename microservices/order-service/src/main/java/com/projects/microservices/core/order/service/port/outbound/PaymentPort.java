package com.projects.microservices.core.order.service.port.outbound;

import com.projects.api.core.payment.Payment;
import com.projects.api.core.payment.PaymentRequest;
import reactor.core.publisher.Mono;

public interface PaymentPort {
    Mono<Payment> authorize(PaymentRequest request);
    Mono<Payment> getPaymentByOrderId(String orderId);
    Mono<Payment> refund(String paymentId, double amount, String reason);
}
