package com.projects.api.core.order;

import java.util.Locale;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public static OrderStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Order status is required");
        }
        return OrderStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
