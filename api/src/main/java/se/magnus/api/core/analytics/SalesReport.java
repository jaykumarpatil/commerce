package se.magnus.api.core.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesReport {
    private String reportId;
    private java.time.LocalDateTime startDate;
    private java.time.LocalDateTime endDate;
    private Double totalSales;
    private Integer orderCount;
    private Double averageOrderValue;
    private java.util.List<DailySales> dailySales;
    private String currency;
}
