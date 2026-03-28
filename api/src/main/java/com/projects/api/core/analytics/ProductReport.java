package com.projects.api.core.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReport {
    private String reportId;
    private java.time.LocalDateTime startDate;
    private java.time.LocalDateTime endDate;
    private Integer totalProductsSold;
    private Double totalRevenue;
    private java.util.List<ProductSales> topSellingProducts;
}
