package com.projects.api.core.shipping;

import java.util.Locale;

public enum ShipmentStatus {
    PENDING,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    RETURNED;

    public static ShipmentStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Shipment status is required");
        }
        return ShipmentStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
