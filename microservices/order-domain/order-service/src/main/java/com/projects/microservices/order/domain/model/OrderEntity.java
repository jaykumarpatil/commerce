package com.projects.microservices.order.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("orders")
public class OrderEntity {
    @Id
    private Long id;
    private String orderId;
    private String userId;
    private String cartId;
    private Double subtotal;
    private Double discountTotal;
    private Double taxAmount;
    private Double shippingCost;
    private Double grandTotal;
    private String status;
    private String shippingAddress;
    private String billingAddress;
    private String paymentMethod;
    private String paymentStatus;
    private String trackingNumber;
    private String carrier;
    private LocalDateTime orderDate;
    private LocalDateTime confirmedDate;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private String createdAt;
    private String updatedAt;
    
    private List<OrderItemEntity> items = new ArrayList<>();
    
    public boolean canCancel() {
        return "PENDING".equals(status) || "CONFIRMED".equals(status);
    }
    
    public boolean transitionTo(String newStatus) {
        String current = this.status;
        if ("PENDING".equals(current)) {
            return "CONFIRMED".equals(newStatus) || "CANCELLED".equals(newStatus);
        }
        if ("CONFIRMED".equals(current)) {
            return "PROCESSING".equals(newStatus) || "CANCELLED".equals(newStatus);
        }
        if ("PROCESSING".equals(current)) {
            return "SHIPPED".equals(newStatus);
        }
        if ("SHIPPED".equals(current)) {
            return "DELIVERED".equals(newStatus);
        }
        return false;
    }
    
    public void addItem(OrderItemEntity item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }
}
