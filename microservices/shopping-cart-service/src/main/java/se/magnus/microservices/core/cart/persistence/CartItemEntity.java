package se.magnus.microservices.core.cart.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cart_items")
public class CartItemEntity {
    @Id
    private String id;
    private String cartItemId;
    private String productId;
    private String productName;
    private String productImage;
    private Double unitPrice;
    private Integer quantity;
    private Integer maxOrderQuantity;
    private boolean inStock;
    private java.util.List<CartItemOptionEntity> options;
    private Double discountAmount;
    private Double totalPrice;
    private String createdAt;
}
