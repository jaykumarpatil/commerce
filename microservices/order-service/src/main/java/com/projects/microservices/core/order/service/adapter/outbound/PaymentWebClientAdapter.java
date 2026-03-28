package com.projects.microservices.core.order.service.adapter.outbound;

import com.projects.api.core.payment.Payment;
import com.projects.api.core.payment.PaymentRequest;
import com.projects.api.core.payment.RefundRequest;
import com.projects.microservices.core.order.service.port.outbound.PaymentPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PaymentWebClientAdapter implements PaymentPort {

    private final WebClient webClient;

    public PaymentWebClientAdapter(
            WebClient.Builder builder,
            @Value("${app.services.payment.base-url:http://payment-service}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<Payment> authorize(PaymentRequest request) {
        return webClient.post()
                .uri("/v1/payments")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Payment.class);
    }


    @Override
    public Mono<Payment> getPaymentByOrderId(String orderId) {
        return webClient.get()
                .uri("/v1/payments/order/{orderId}", orderId)
                .retrieve()
                .bodyToMono(Payment.class);
    }

    @Override
    public Mono<Payment> refund(String paymentId, double amount, String reason) {
        RefundRequest request = new RefundRequest(paymentId, amount, reason);
        return webClient.post()
                .uri("/v1/payments/{paymentId}/refund", paymentId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Payment.class);
    }
}
