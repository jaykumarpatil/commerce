package se.magnus.microservices.payment.application.port.outbound;

public record PaymentResult(String transactionId, boolean success, String errorMessage) {}
