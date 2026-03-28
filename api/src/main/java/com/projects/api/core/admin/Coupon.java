package com.projects.api.core.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    private String couponId;
    private String code;
    private String description;
    private Double discountPercent;
    private Double minimumOrderAmount;
    private Integer maxUses;
    private Integer currentUses;
    private boolean active;
    private java.time.LocalDateTime startDate;
    private java.time.LocalDateTime endDate;
    private String createdAt;
}
