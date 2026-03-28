package com.projects.microservices.payment.application.port.outbound;

public record RefundResult(String refundId, boolean success, String errorMessage) {}
