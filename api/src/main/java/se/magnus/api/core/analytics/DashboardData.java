package se.magnus.api.core.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardData {
    private Double totalSales;
    private Integer orderCount;
    private Integer customerCount;
    private Integer productCount;
    private Double cartAbandonmentRate;
    private Double conversionRate;
}
