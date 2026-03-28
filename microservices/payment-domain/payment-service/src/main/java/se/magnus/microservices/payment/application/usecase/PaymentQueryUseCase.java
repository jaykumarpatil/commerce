package se.magnus.microservices.payment.application.usecase;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import se.magnus.api.core.payment.Payment;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.microservices.payment.application.port.inbound.PaymentQueryPort;
import se.magnus.microservices.payment.application.port.outbound.PaymentRepositoryPort;
import se.magnus.microservices.payment.domain.model.PaymentEntity;

@Service
public class PaymentQueryUseCase implements PaymentQueryPort {

    private final PaymentRepositoryPort repository;

    public PaymentQueryUseCase(PaymentRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Payment> getPayment(String paymentId) {
        return repository.findByPaymentId(paymentId)
                .switchIfEmpty(Mono.error(new NotFoundException("Payment not found")))
                .map(this::mapToApi);
    }

    @Override
    public Mono<Payment> getPaymentByOrderId(String orderId) {
        return repository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Payment not found for order")))
                .map(this::mapToApi);
    }

    private Payment mapToApi(PaymentEntity entity) {
        if (entity == null) return null;
        Payment payment = new Payment();
        payment.setPaymentId(entity.getPaymentId());
        payment.setOrderId(entity.getOrderId());
        payment.setAmount(entity.getAmount());
        payment.setCurrency(entity.getCurrency());
        payment.setPaymentMethod(entity.getPaymentMethod());
        payment.setPaymentStatus(entity.getPaymentStatus());
        payment.setTransactionId(entity.getTransactionId());
        payment.setCardLastFour(entity.getCardLastFour());
        payment.setCardBrand(entity.getCardBrand());
        payment.setPaymentDate(entity.getPaymentDate());
        payment.setFailureReason(entity.getFailureReason());
        return payment;
    }
}
