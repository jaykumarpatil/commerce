package com.projects.microservices.core.analytics.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("product_reports")
public class ProductReportEntity {
    @Id
    private Long id;
    private String reportId;
    private java.time.LocalDateTime startDate;
    private java.time.LocalDateTime endDate;
    private Integer totalProductsSold;
    private Double totalRevenue;
}
