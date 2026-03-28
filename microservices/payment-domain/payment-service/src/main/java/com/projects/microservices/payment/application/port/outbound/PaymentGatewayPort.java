package com.projects.microservices.payment.application.port.outbound;

import reactor.core.publisher.Mono;

public interface PaymentGatewayPort {
    Mono<PaymentResult> charge(PaymentRequest request);
    Mono<RefundResult> refund(String transactionId, Double amount);
}
