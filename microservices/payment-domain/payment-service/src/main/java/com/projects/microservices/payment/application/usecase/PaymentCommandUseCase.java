package com.projects.microservices.payment.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.projects.api.core.payment.Payment;
import com.projects.api.exceptions.BadRequestException;
import com.projects.api.exceptions.InvalidInputException;
import com.projects.api.exceptions.NotFoundException;
import com.projects.microservices.payment.application.port.inbound.PaymentCommandPort;
import com.projects.microservices.payment.application.port.outbound.PaymentEventPublisherPort;
import com.projects.microservices.payment.application.port.outbound.PaymentGatewayPort;
import com.projects.microservices.payment.application.port.outbound.PaymentRepositoryPort;
import com.projects.microservices.payment.application.port.outbound.PaymentRequest;
import com.projects.microservices.payment.domain.event.PaymentEvent;
import com.projects.microservices.payment.domain.model.PaymentEntity;
import com.projects.microservices.payment.domain.service.PaymentDomainService;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentCommandUseCase implements PaymentCommandPort {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentCommandUseCase.class);

    private final PaymentRepositoryPort repository;
    private final PaymentGatewayPort paymentGateway;
    private final PaymentEventPublisherPort eventPublisher;
    private final PaymentDomainService domainService;

    public PaymentCommandUseCase(PaymentRepositoryPort repository, PaymentGatewayPort paymentGateway,
                               PaymentEventPublisherPort eventPublisher, PaymentDomainService domainService) {
        this.repository = repository;
        this.paymentGateway = paymentGateway;
        this.eventPublisher = eventPublisher;
        this.domainService = domainService;
    }

    @Override
    public Mono<Payment> processPayment(String orderId, Double amount, String currency, String paymentMethod) {
        if (orderId == null || orderId.isBlank()) {
            return Mono.error(new InvalidInputException("Order ID is required"));
        }
        if (amount == null || amount <= 0) {
            return Mono.error(new InvalidInputException("Valid amount is required"));
        }

        PaymentEntity payment = new PaymentEntity();
        payment.setPaymentMethod(paymentMethod);
        domainService.createPayment(payment, orderId, amount, currency);

        return repository.save(payment)
                .doOnSuccess(p -> eventPublisher.publish(new PaymentEvent.PaymentInitiated(
                        p.getPaymentId(), orderId, amount, currency, Instant.now())))
                .flatMap(p -> processWithGateway(p))
                .map(this::mapToApi);
    }

    private Mono<PaymentEntity> processWithGateway(PaymentEntity payment) {
        return paymentGateway.charge(new PaymentRequest(
                        payment.getOrderId(), payment.getAmount(), payment.getCurrency()))
                .flatMap(result -> {
                    if (result.success()) {
                        domainService.markAsCompleted(payment, result.transactionId());
                        eventPublisher.publish(new PaymentEvent.PaymentCompleted(
                                payment.getPaymentId(), payment.getOrderId(), 
                                result.transactionId(), payment.getAmount(), Instant.now()));
                        return repository.save(payment);
                    } else {
                        domainService.markAsFailed(payment, result.errorMessage());
                        eventPublisher.publish(new PaymentEvent.PaymentFailed(
                                payment.getPaymentId(), payment.getOrderId(), 
                                result.errorMessage(), Instant.now()));
                        return repository.save(payment);
                    }
                });
    }

    @Override
    public Mono<Payment> refundPayment(String paymentId, Double amount) {
        return repository.findByPaymentId(paymentId)
                .switchIfEmpty(Mono.error(new NotFoundException("Payment not found")))
                .flatMap(payment -> {
                    if (!domainService.canRefund(payment)) {
                        return Mono.error(new BadRequestException("Payment cannot be refunded"));
                    }
                    eventPublisher.publish(new PaymentEvent.RefundInitiated(
                            paymentId, payment.getOrderId(), payment.getAmount(), Instant.now()));
                    
                    return paymentGateway.refund(payment.getTransactionId(), amount)
                            .flatMap(result -> {
                                if (result.success()) {
                                    domainService.markAsRefunded(payment);
                                    eventPublisher.publish(new PaymentEvent.RefundCompleted(
                                            paymentId, payment.getOrderId(), 
                                            result.refundId(), Instant.now()));
                                    return repository.save(payment);
                                } else {
                                    return Mono.error(new BadRequestException("Refund failed: " + result.errorMessage()));
                                }
                            });
                })
                .map(this::mapToApi);
    }

    @Override
    public Mono<Payment> updatePaymentStatus(String paymentId, String status) {
        return repository.findByPaymentId(paymentId)
                .switchIfEmpty(Mono.error(new NotFoundException("Payment not found")))
                .flatMap(payment -> {
                    payment.setPaymentStatus(status);
                    return repository.save(payment);
                })
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
        payment.setIs3DSecure(entity.getIs3DSecure());
        return payment;
    }
}
