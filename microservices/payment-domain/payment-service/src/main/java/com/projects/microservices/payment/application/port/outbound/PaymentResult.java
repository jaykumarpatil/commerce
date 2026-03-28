package com.projects.microservices.payment.application.port.outbound;

public record PaymentResult(String transactionId, boolean success, String errorMessage) {}
