package com.projects.api.core.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsReport {
    private String reportType;
    private java.time.LocalDateTime startDate;
    private java.time.LocalDateTime endDate;
    private Integer totalValue;
    private Double averageValue;
    private String period;
}
