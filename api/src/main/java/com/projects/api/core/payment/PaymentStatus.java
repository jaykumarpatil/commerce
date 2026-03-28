package com.projects.api.core.payment;

import java.util.Locale;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    PARTIALLY_REFUNDED;

    public static PaymentStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Payment status is required");
        }
        return PaymentStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
