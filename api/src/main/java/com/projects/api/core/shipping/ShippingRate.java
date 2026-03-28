package com.projects.api.core.shipping;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingRate {
    private String rateId;
    private String orderId;
    private Double weight;
    private Double basePrice;
    private Double weightPrice;
    private Double distancePrice;
    private Double totalAmount;
    private String carrier;
    private String serviceLevel; // STANDARD, EXPEDITED, OVERNIGHT
    private Integer estimatedDeliveryDays;
    private String estimatedDeliveryDate;
    private Boolean isExpress;
}
