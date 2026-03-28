package com.projects.microservices.cart.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "carts")
public class CartEntity {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String cartId;
    
    @Indexed
    private String userId;
    
    private List<CartItemEntity> items = new ArrayList<>();
    private Double subtotal;
    private Double discountTotal;
    private Double taxAmount;
    private Double shippingCost;
    private Double grandTotal;
    private Integer itemTotalCount;
    private String createdAt;
    private String updatedAt;
    
    public void addItem(CartItemEntity item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }
    
    public void removeItem(String cartItemId) {
        if (items != null) {
            items.removeIf(item -> item.getCartItemId().equals(cartItemId));
        }
    }
    
    public CartItemEntity findItem(String cartItemId) {
        if (items == null) return null;
        return items.stream()
                .filter(item -> item.getCartItemId().equals(cartItemId))
                .findFirst()
                .orElse(null);
    }
    
    public void updateItem(String cartItemId, CartItemEntity updatedItem) {
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getCartItemId().equals(cartItemId)) {
                    items.set(i, updatedItem);
                    return;
                }
            }
        }
    }
}
