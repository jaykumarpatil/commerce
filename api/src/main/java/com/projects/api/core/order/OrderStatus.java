package com.projects.api.core.order;

public enum OrderStatus {
    CREATED,
    CONFIRMED,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    FAILED,
    REFUNDED,

    /** @deprecated use CREATED */
    @Deprecated PENDING,
    /** @deprecated use CONFIRMED */
    @Deprecated INVENTORY_RESERVED,
    /** @deprecated use PAID */
    @Deprecated PAYMENT_AUTHORIZED;

    public OrderStatus normalize() {
        return switch (this) {
            case PENDING -> CREATED;
            case INVENTORY_RESERVED -> CONFIRMED;
            case PAYMENT_AUTHORIZED -> PAID;
            default -> this;
        };
    }
}
