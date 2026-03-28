package com.projects.microservices.core.analytics.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("sales_reports")
public class SalesReportEntity {
    @Id
    private Long id;
    private String reportId;
    private java.time.LocalDateTime startDate;
    private java.time.LocalDateTime endDate;
    private Double totalSales;
    private Integer orderCount;
    private Double averageOrderValue;
    private String currency;
}
