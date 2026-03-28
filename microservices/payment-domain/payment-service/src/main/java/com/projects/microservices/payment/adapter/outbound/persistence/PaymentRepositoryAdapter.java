package com.projects.microservices.payment.adapter.outbound.persistence;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.projects.microservices.payment.application.port.outbound.PaymentRepositoryPort;
import com.projects.microservices.payment.domain.model.PaymentEntity;

@Service
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final PaymentR2dbcRepository r2dbcRepository;

    public PaymentRepositoryAdapter(PaymentR2dbcRepository r2dbcRepository) {
        this.r2dbcRepository = r2dbcRepository;
    }

    @Override
    public Mono<PaymentEntity> save(PaymentEntity payment) {
        return r2dbcRepository.save(payment);
    }

    @Override
    public Mono<PaymentEntity> findByPaymentId(String paymentId) {
        return r2dbcRepository.findByPaymentId(paymentId);
    }

    @Override
    public Mono<PaymentEntity> findByOrderId(String orderId) {
        return r2dbcRepository.findByOrderId(orderId);
    }
}
