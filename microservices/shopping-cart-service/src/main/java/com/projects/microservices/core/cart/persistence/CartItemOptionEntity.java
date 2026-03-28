package com.projects.microservices.core.cart.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cart_item_options")
public class CartItemOptionEntity {
    @Id
    private String id;
    private String optionName;
    private String optionValue;
}
