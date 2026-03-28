package com.projects.microservices.payment.application.port.outbound;

public record PaymentRequest(String orderId, Double amount, String currency) {}
