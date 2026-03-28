package com.projects.api.core.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Variant {
    private String variantId;
    private String productId;
    private String name;
    private String sku;
    private Double price;
    private Integer stockQuantity;
    private boolean active;
    private java.util.List<AttributeValue> attributes;
}
