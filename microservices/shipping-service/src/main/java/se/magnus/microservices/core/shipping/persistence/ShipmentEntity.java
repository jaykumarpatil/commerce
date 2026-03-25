package se.magnus.microservices.core.shipping.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("shipments")
public class ShipmentEntity {
    @Id
    private Long id;
    private String shipmentId;
    private String orderId;
    private String trackingNumber;
    private String carrier;
    private String status; // PENDING, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED
    private String shippingAddress;
    private Double weight;
    private Double dimensions;
    private java.time.LocalDateTime estimatedDeliveryDate;
    private java.time.LocalDateTime actualDeliveryDate;
    private String deliveryConfirmation;
    private String createdAt;
}
