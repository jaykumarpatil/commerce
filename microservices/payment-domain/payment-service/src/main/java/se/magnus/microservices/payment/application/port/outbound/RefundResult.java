package se.magnus.microservices.payment.application.port.outbound;

public record RefundResult(String refundId, boolean success, String errorMessage) {}
