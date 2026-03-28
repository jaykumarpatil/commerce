package com.projects.microservices.core.cart.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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
    
    private java.util.List<CartItemEntity> items;
    private Double subtotal;
    private Double discountTotal;
    private Double taxAmount;
    private Double shippingCost;
    private Double grandTotal;
    private Integer itemTotalCount;
    private String createdAt;
    private String updatedAt;
}
