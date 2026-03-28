package com.projects.api.core.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String cartItemId;
    private String productId;
    private String productName;
    private String productImage;
    private Double unitPrice;
    private Integer quantity;
    private Integer maxOrderQuantity;
    private boolean inStock;
    private java.util.List<CartItemOption> options;
    private Double discountAmount;
    private Double totalPrice;
    private String createdAt;
}
