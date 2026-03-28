package se.magnus.microservices.cart.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

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
    private List<CartItemOptionEntity> options = new ArrayList<>();
    private Double discountAmount;
    private Double totalPrice;
    private String createdAt;
    
    public Double calculateTotalPrice() {
        if (unitPrice == null || quantity == null) {
            return 0.0;
        }
        double baseTotal = unitPrice * quantity;
        double discount = discountAmount != null ? discountAmount : 0.0;
        return baseTotal - discount;
    }
}
