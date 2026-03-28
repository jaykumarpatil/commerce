package com.projects.microservices.payment.application.port.outbound;

import com.projects.microservices.payment.domain.model.PaymentEntity;
import reactor.core.publisher.Mono;

public interface PaymentRepositoryPort {
    Mono<PaymentEntity> save(PaymentEntity payment);
    Mono<PaymentEntity> findByPaymentId(String paymentId);
    Mono<PaymentEntity> findByOrderId(String orderId);
}
