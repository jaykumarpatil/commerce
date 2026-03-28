package com.projects.microservices.core.payment.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PaymentRepository extends ReactiveCrudRepository<PaymentEntity, Long> {
    Mono<PaymentEntity> findByPaymentId(String paymentId);
    Mono<PaymentEntity> findByOrderId(String orderId);
    Mono<PaymentEntity> findByTransactionId(String transactionId);
}
