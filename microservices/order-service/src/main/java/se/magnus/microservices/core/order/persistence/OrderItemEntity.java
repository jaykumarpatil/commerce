package se.magnus.microservices.core.order.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("order_items")
public class OrderItemEntity {
    @Id
    private Long id;
    private String orderItemId;
    private String orderId;
    private String productId;
    private String productName;
    private String productImage;
    private Double unitPrice;
    private Integer quantity;
    private Double discountAmount;
    private Double totalPrice;
    private String status;
}
