package com.projects.api.core.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderId;
    private String userId;
    private String cartId;
    private java.util.List<OrderItem> items;
    private Double subtotal;
    private Double discountTotal;
    private Double taxAmount;
    private Double shippingCost;
    private Double grandTotal;
    private String status; // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    private String shippingAddress;
    private String billingAddress;
    private String paymentMethod;
    private String paymentStatus; // PENDING, COMPLETED, FAILED, REFUNDED
    private String trackingNumber;
    private String carrier;
    private java.time.LocalDateTime orderDate;
    private java.time.LocalDateTime confirmedDate;
    private java.time.LocalDateTime shippedDate;
    private java.time.LocalDateTime deliveredDate;
    private String createdAt;
    private String updatedAt;
}
