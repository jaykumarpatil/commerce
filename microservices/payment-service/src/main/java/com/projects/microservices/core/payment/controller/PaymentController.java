package com.projects.microservices.core.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import com.projects.api.core.payment.*;
import com.projects.util.http.ServiceUtil;

@RestController
public class PaymentController implements PaymentService {

    private final PaymentService paymentService;
    private final ServiceUtil serviceUtil;

    @Autowired
    public PaymentController(PaymentService paymentService, ServiceUtil serviceUtil) {
        this.paymentService = paymentService;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Payment> processPayment(PaymentRequest request) {
        return paymentService.processPayment(request);
    }

    @Override
    public Mono<Payment> getPayment(String paymentId) {
        return paymentService.getPayment(paymentId);
    }

    @Override
    public Mono<Payment> getPaymentByOrderId(String orderId) {
        return paymentService.getPaymentByOrderId(orderId);
    }

    @Override
    public Mono<Payment> refundPayment(String paymentId, RefundRequest request) {
        return paymentService.refundPayment(paymentId, request);
    }

    @Override
    public Mono<Payment> updatePaymentStatus(String paymentId, PaymentStatus status) {
        return paymentService.updatePaymentStatus(paymentId, status);
    }

    @Override
    public Mono<Void> handleWebhook(String payload, String signature) {
        return paymentService.handleWebhook(payload, signature);
    }
}
