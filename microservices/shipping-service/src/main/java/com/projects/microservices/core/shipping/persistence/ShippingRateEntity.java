package com.projects.microservices.core.shipping.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("shipping_rates")
public class ShippingRateEntity {
    @Id
    private Long id;
    private String rateId;
    private String orderId;
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
