package com.projects.microservices.core.admin.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("coupons")
public class CouponEntity {
    @Id
    private Long id;
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
