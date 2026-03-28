package se.magnus.microservices.payment.application.port.outbound;

import se.magnus.microservices.payment.domain.model.PaymentEntity;
import reactor.core.publisher.Mono;

public interface PaymentRepositoryPort {
    Mono<PaymentEntity> save(PaymentEntity payment);
    Mono<PaymentEntity> findByPaymentId(String paymentId);
    Mono<PaymentEntity> findByOrderId(String orderId);
}
