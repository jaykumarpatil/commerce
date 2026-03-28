package com.projects.microservices.core.inventory.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("stock_reservations")
public class StockReservationEntity {
    @Id
    private Long id;
    private String reservationId;
    private String orderId;
    private String productId;
    private Integer quantity;
    private String status; // PENDING, CONFIRMED, CANCELLED
    private java.time.LocalDateTime reservedAt;
    private java.time.LocalDateTime confirmedAt;
    private java.time.LocalDateTime cancelledAt;
}
