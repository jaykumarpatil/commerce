package com.projects.api.core.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailySales {
    private java.time.LocalDateTime date;
    private Double salesAmount;
    private Integer orderCount;
}
