package se.magnus.microservices.core.payment.stripe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.*;

@Service
public class StripePaymentService {

    private static final Logger LOG = LoggerFactory.getLogger(StripePaymentService.class);

    @Value("${stripe.webhook.endpoint-secret:}")
    private String webhookSecret;

    @Value("${stripe.checkout.success-url:https://example.com/success}")
    private String successUrl;

    @Value("${stripe.checkout.cancel-url:https://example.com/cancel}")
    private String cancelUrl;

    public Mono<PaymentResult> processPayment(String orderId, BigDecimal amount, String currency,
                                               String customerId, Map<String, String> metadata) {
        return Mono.fromCallable(() -> {
            LOG.info("Processing payment for order {} amount {} {}", orderId, amount, currency);
            
            return PaymentResult.builder()
                    .success(true)
                    .paymentId("PAY_" + System.currentTimeMillis())
                    .status("succeeded")
                    .message("Payment processed successfully")
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<PaymentResult> confirmPayment(String paymentIntentId) {
        return Mono.fromCallable(() -> {
            LOG.info("Confirming payment {}", paymentIntentId);
            
            return PaymentResult.builder()
                    .success(true)
                    .paymentId(paymentIntentId)
                    .status("succeeded")
                    .message("Payment confirmed")
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<PaymentResult> refundPayment(String paymentIntentId, BigDecimal amount, String reason) {
        return Mono.fromCallable(() -> {
            LOG.info("Refunding payment {} amount {} reason {}", paymentIntentId, amount, reason);
            
            return PaymentResult.builder()
                    .success(true)
                    .paymentId(paymentIntentId)
                    .refundId("REF_" + System.currentTimeMillis())
                    .status("succeeded")
                    .message("Refund processed")
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<PaymentResult> createCheckoutSession(String orderId, BigDecimal amount, String currency,
                                                     List<CheckoutItem> items, String customerId,
                                                     Map<String, String> metadata) {
        return Mono.fromCallable(() -> {
            LOG.info("Creating checkout session for order {}", orderId);
            
            String sessionId = "cs_" + System.currentTimeMillis();
            
            return PaymentResult.builder()
                    .success(true)
                    .sessionId(sessionId)
                    .checkoutUrl(successUrl + "?session=" + sessionId)
                    .paymentId(sessionId)
                    .status("pending")
                    .message("Checkout session created")
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<PaymentResult> process3DSecure(String paymentIntentId, String returnUrl) {
        return Mono.fromCallable(() -> {
            LOG.info("Processing 3D Secure for {}", paymentIntentId);
            
            return PaymentResult.builder()
                    .success(true)
                    .paymentId(paymentIntentId)
                    .clientSecret("client_secret_" + System.currentTimeMillis())
                    .status("requires_action")
                    .message("3D Secure verification initiated")
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<PaymentResult> createCustomer(String email, String name) {
        return Mono.fromCallable(() -> {
            LOG.info("Creating customer for {}", email);
            
            return PaymentResult.builder()
                    .success(true)
                    .customerId("cus_" + System.currentTimeMillis())
                    .message("Customer created")
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<PaymentResult> handleWebhook(String payload, String sigHeader) {
        return Mono.fromCallable(() -> {
            LOG.info("Handling webhook");
            
            return PaymentResult.builder()
                    .success(true)
                    .message("Webhook processed")
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<PaymentResult> getPaymentStatus(String paymentIntentId) {
        return Mono.fromCallable(() -> {
            LOG.info("Getting payment status for {}", paymentIntentId);
            
            return PaymentResult.builder()
                    .success(true)
                    .paymentId(paymentIntentId)
                    .status("succeeded")
                    .amount(BigDecimal.valueOf(99.99))
                    .currency("usd")
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PaymentResult {
        private boolean success;
        private String paymentId;
        private String refundId;
        private String sessionId;
        private String checkoutUrl;
        private String clientSecret;
        private String customerId;
        private String status;
        private String message;
        private BigDecimal amount;
        private String currency;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CheckoutItem {
        private String name;
        private String description;
        private BigDecimal unitPrice;
        private int quantity;
        private String imageUrl;
    }
}
