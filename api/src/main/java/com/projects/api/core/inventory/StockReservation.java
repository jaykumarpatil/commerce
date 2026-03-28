package com.projects.api.core.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReservation {
    private String reservationId;
    private String orderId;
    private String productId;
    private Integer quantity;
    private String status; // PENDING, CONFIRMED, CANCELLED
    private java.time.LocalDateTime reservedAt;
    private java.time.LocalDateTime confirmedAt;
    private java.time.LocalDateTime cancelledAt;
}
