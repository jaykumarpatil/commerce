package com.projects.microservices.core.payment.service;

import static java.util.logging.Level.FINE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Refund;
import com.stripe.param.ChargeCreateParams;
import org.slf4j.Logger;

import java.util.Map;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.projects.api.core.payment.*;
import com.projects.api.exceptions.BadRequestException;
import com.projects.api.exceptions.InvalidInputException;
import com.projects.api.exceptions.NotFoundException;
import com.projects.microservices.core.payment.persistence.PaymentEntity;
import com.projects.microservices.core.payment.persistence.PaymentRepository;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ObjectMapper objectMapper;

    @Value("${stripe.api-key:}")
    private String stripeApiKey;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentMapper paymentMapper, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Payment> processPayment(PaymentRequest request) {
        if (request.getOrderId() == null || request.getOrderId().isEmpty()) {
            return Mono.error(new InvalidInputException("Order ID is required"));
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            return Mono.error(new InvalidInputException("Valid amount is required"));
        }

        // Initialize Stripe
        if (stripeApiKey == null || stripeApiKey.isEmpty()) {
            LOG.warn("Stripe API key not configured, using mock payment");
            return createMockPayment(request);
        }

        Stripe.apiKey = stripeApiKey;

        try {
            // Create charge with Stripe
            ChargeCreateParams params = ChargeCreateParams.builder()
                    .setAmount((long) (request.getAmount() * 100)) // Convert to cents
                    .setCurrency(request.getCurrency().toLowerCase())
                    .setDescription("Payment for order " + request.getOrderId())
                    .setSource(request.getCardToken()) // Token from Stripe.js
                    .build();

            Charge charge = Charge.create(params);

            PaymentEntity entity = paymentMapper.apiToEntity(new Payment());
            entity.setPaymentId(java.util.UUID.randomUUID().toString());
            entity.setOrderId(request.getOrderId());
            entity.setAmount(request.getAmount());
            entity.setCurrency(request.getCurrency());
            entity.setPaymentMethod(request.getPaymentMethod());
            entity.setPaymentStatus("COMPLETED");
            entity.setTransactionId(charge.getId());
            
            entity.setCardLastFour("4242");
            entity.setCardBrand("visa");
            
            entity.setPaymentDate(java.time.LocalDateTime.now());
            entity.setIs3DSecure(request.getIs3DSecure());

            return paymentRepository.save(entity)
                    .log(LOG.getName(), FINE)
                    .map(paymentMapper::entityToApi);

        } catch (StripeException e) {
            LOG.error("Payment failed: {}", e.getMessage());
            
            PaymentEntity entity = paymentMapper.apiToEntity(new Payment());
            entity.setPaymentId(java.util.UUID.randomUUID().toString());
            entity.setOrderId(request.getOrderId());
            entity.setAmount(request.getAmount());
            entity.setCurrency(request.getCurrency());
            entity.setPaymentMethod(request.getPaymentMethod());
            entity.setPaymentStatus("FAILED");
            entity.setFailureReason(e.getMessage());
            
            return paymentRepository.save(entity)
                    .log(LOG.getName(), FINE)
                    .map(paymentMapper::entityToApi);
        }
    }

    @Override
    public Mono<Payment> getPayment(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
                .switchIfEmpty(Mono.error(new NotFoundException("Payment not found: " + paymentId)))
                .map(paymentMapper::entityToApi);
    }

    @Override
    public Mono<Payment> getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Payment not found for order: " + orderId)))
                .map(paymentMapper::entityToApi);
    }

    @Override
    public Mono<Payment> refundPayment(String paymentId, RefundRequest request) {
        return paymentRepository.findByPaymentId(paymentId)
                .switchIfEmpty(Mono.error(new NotFoundException("Payment not found: " + paymentId)))
                .flatMap(entity -> {
                    if (!"COMPLETED".equals(entity.getPaymentStatus())) {
                        return Mono.error(new BadRequestException("Only completed payments can be refunded"));
                    }

                    if (entity.getTransactionId() == null || entity.getTransactionId().isEmpty()) {
                        return Mono.error(new BadRequestException("No transaction ID for refund"));
                    }

                    // Initialize Stripe
                    if (stripeApiKey == null || stripeApiKey.isEmpty()) {
                        LOG.warn("Stripe API key not configured, using mock refund");
                        entity.setPaymentStatus("REFUNDED");
                        return paymentRepository.save(entity)
                                .log(LOG.getName(), FINE)
                                .map(paymentMapper::entityToApi);
                    }

                    Stripe.apiKey = stripeApiKey;

                    try {
                        Map<String, Object> params = new java.util.HashMap<>();
                        params.put("charge", entity.getTransactionId());
                        Refund refund = Refund.create(params);

                        entity.setPaymentStatus("REFUNDED");
                        return paymentRepository.save(entity)
                                .log(LOG.getName(), FINE)
                                .map(paymentMapper::entityToApi);

                    } catch (StripeException e) {
                        LOG.error("Refund failed: {}", e.getMessage());
                        return Mono.error(new BadRequestException("Refund failed: " + e.getMessage()));
                    }
                });
    }

    @Override
    public Mono<Payment> updatePaymentStatus(String paymentId, String status) {
        return paymentRepository.findByPaymentId(paymentId)
                .switchIfEmpty(Mono.error(new NotFoundException("Payment not found: " + paymentId)))
                .flatMap(entity -> {
                    entity.setPaymentStatus(status);
                    return paymentRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(paymentMapper::entityToApi);
                });
    }

    @Override
    public Mono<Void> handleWebhook(String payload, String signature) {
        if (payload == null || payload.isBlank()) {
            return Mono.error(new InvalidInputException("Webhook payload is required"));
        }

        return Mono.fromCallable(() -> objectMapper.readTree(payload))
                .onErrorMap(ex -> new BadRequestException("Invalid webhook payload"))
                .flatMap(this::handleWebhookEvent)
                .then();
    }

    private Mono<Payment> createMockPayment(PaymentRequest request) {
        PaymentEntity entity = paymentMapper.apiToEntity(new Payment());
        entity.setPaymentId(java.util.UUID.randomUUID().toString());
        entity.setOrderId(request.getOrderId());
        entity.setAmount(request.getAmount());
        entity.setCurrency(request.getCurrency());
        entity.setPaymentMethod(request.getPaymentMethod());
        entity.setPaymentStatus("COMPLETED");
        entity.setTransactionId("mock-" + java.util.UUID.randomUUID().toString());
        entity.setCardLastFour("1234");
        entity.setPaymentDate(java.time.LocalDateTime.now());

        return paymentRepository.save(entity)
                .log(LOG.getName(), FINE)
                .map(paymentMapper::entityToApi);
    }

    private Mono<PaymentEntity> handleWebhookEvent(JsonNode eventNode) {
        String transactionId = asText(eventNode.at("/data/object/id"));
        String status = normalizeStatus(asText(eventNode.at("/data/object/status")));

        if (transactionId == null || transactionId.isBlank()) {
            return Mono.error(new BadRequestException("Webhook payload missing transaction ID"));
        }

        if (status == null || status.isBlank()) {
            return Mono.error(new BadRequestException("Webhook payload missing payment status"));
        }

        return paymentRepository.findByTransactionId(transactionId)
                .switchIfEmpty(Mono.error(new NotFoundException("Payment not found for transaction: " + transactionId)))
                .flatMap(entity -> {
                    entity.setPaymentStatus(status);
                    if ("FAILED".equals(status) && eventNode.at("/data/object/failure_message").isTextual()) {
                        entity.setFailureReason(eventNode.at("/data/object/failure_message").asText());
                    }
                    return paymentRepository.save(entity).log(LOG.getName(), FINE);
                });
    }

    private String asText(JsonNode node) {
        return node != null && !node.isMissingNode() && !node.isNull() ? node.asText() : null;
    }

    private String normalizeStatus(String providerStatus) {
        if (providerStatus == null) return null;
        return switch (providerStatus.toLowerCase()) {
            case "succeeded", "paid", "captured" -> "COMPLETED";
            case "pending", "processing", "requires_action" -> "PENDING";
            case "refunded", "partially_refunded" -> "REFUNDED";
            case "failed", "canceled" -> "FAILED";
            default -> providerStatus.toUpperCase();
        };
    }
}
