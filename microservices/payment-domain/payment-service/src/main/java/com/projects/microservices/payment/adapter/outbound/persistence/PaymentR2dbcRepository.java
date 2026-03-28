package com.projects.microservices.payment.adapter.outbound.persistence;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import com.projects.microservices.payment.domain.model.PaymentEntity;

@Repository
public interface PaymentR2dbcRepository extends R2dbcRepository<PaymentEntity, Long> {
    Mono<PaymentEntity> findByPaymentId(String paymentId);
    Mono<PaymentEntity> findByOrderId(String orderId);
}
