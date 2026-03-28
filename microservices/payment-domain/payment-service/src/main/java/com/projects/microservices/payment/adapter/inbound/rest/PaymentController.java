package com.projects.microservices.payment.adapter.inbound.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import com.projects.api.core.payment.Payment;
import com.projects.microservices.payment.application.port.inbound.PaymentCommandPort;
import com.projects.microservices.payment.application.port.inbound.PaymentQueryPort;

@RestController
public class PaymentController {

    private final PaymentCommandPort commandPort;
    private final PaymentQueryPort queryPort;

    @Autowired
    public PaymentController(PaymentCommandPort commandPort, PaymentQueryPort queryPort) {
        this.commandPort = commandPort;
        this.queryPort = queryPort;
    }

    public Mono<Payment> processPayment(String orderId, Double amount, String currency, String paymentMethod) {
        return commandPort.processPayment(orderId, amount, currency, paymentMethod);
    }

    public Mono<Payment> getPayment(String paymentId) {
        return queryPort.getPayment(paymentId);
    }

    public Mono<Payment> getPaymentByOrderId(String orderId) {
        return queryPort.getPaymentByOrderId(orderId);
    }

    public Mono<Payment> refundPayment(String paymentId, Double amount) {
        return commandPort.refundPayment(paymentId, amount);
    }

    public Mono<Payment> updatePaymentStatus(String paymentId, String status) {
        return commandPort.updatePaymentStatus(paymentId, status);
    }
}
