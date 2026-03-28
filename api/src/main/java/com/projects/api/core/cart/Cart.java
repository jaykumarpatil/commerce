package com.projects.api.core.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    private String cartId;
    private String userId;
    private java.util.List<CartItem> items;
    private Double subtotal;
    private Double discountTotal;
    private Double taxAmount;
    private Double shippingCost;
    private Double grandTotal;
    private Integer itemTotalCount;
    private String createdAt;
    private String updatedAt;
}
