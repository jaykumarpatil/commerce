package com.projects.api.core.order;

public enum OrderStatus {
    PENDING,
    INVENTORY_RESERVED,
    PAYMENT_AUTHORIZED,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    FAILED,
    REFUNDED
}
