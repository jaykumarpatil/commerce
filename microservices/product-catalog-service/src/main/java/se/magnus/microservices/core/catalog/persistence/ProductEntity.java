package se.magnus.microservices.core.catalog.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("products")
public class ProductEntity {
    @Id
    private Long id;
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
    private String imagesJson;
    private Integer stockQuantity;
    private Integer minOrderQuantity;
    private Integer maxOrderQuantity;
    private boolean inStock;
    private boolean featured;
    private boolean active;
    private String categoryId;
    private String createdAt;
    private String updatedAt;
}
