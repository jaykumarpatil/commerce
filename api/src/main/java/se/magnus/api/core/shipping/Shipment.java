package se.magnus.api.core.shipping;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {
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
