package se.magnus.api.core.payment;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaymentService {

    @PostMapping("/v1/payments")
    Mono<Payment> processPayment(PaymentRequest request);

    @GetMapping("/v1/payments/{paymentId}")
    Mono<Payment> getPayment(String paymentId);

    @GetMapping("/v1/payments/order/{orderId}")
    Mono<Payment> getPaymentByOrderId(String orderId);

    @PostMapping("/v1/payments/{paymentId}/refund")
    Mono<Payment> refundPayment(String paymentId, RefundRequest request);

    @PatchMapping("/v1/payments/{paymentId}/status")
    Mono<Payment> updatePaymentStatus(String paymentId, String status);

    @GetMapping("/v1/payments/webhook")
    Mono<Void> handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String signature);
}
