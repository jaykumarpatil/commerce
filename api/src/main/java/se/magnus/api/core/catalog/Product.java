package se.magnus.api.core.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String productId;
    private String name;
    private String description;
    private String shortDescription;
    private String slug;
    private String sku;
    private String barcode;
    private Double price;
    private Double originalPrice;
    private Double discountPercent;
    private String imageUrl;
    private String mainImage;
    private java.util.List<String> images;
    private Integer stockQuantity;
    private Integer minOrderQuantity;
    private Integer maxOrderQuantity;
    private boolean inStock;
    private boolean featured;
    private boolean active;
    private String categoryId;
    private Category category;
    private java.util.List<Variant> variants;
    private java.util.List<Attribute> attributes;
    private String createdAt;
    private String updatedAt;
}
