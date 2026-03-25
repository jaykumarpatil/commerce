package se.magnus.microservices.core.order.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

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
