package se.magnus.api.core.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private String orderItemId;
    private String productId;
    private String productName;
    private String productImage;
    private Double unitPrice;
    private Integer quantity;
    private Double discountAmount;
    private Double totalPrice;
    private String status;
}
