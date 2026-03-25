package se.magnus.api.core.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSales {
    private String productId;
    private String productName;
    private Integer quantitySold;
    private Double revenue;
}
