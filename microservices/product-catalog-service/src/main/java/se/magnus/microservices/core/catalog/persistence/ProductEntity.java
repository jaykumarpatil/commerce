package se.magnus.microservices.core.catalog.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import se.magnus.api.core.catalog.Attribute;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
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
    
    @Column("images")
    private List<String> images;
    
    private Integer stockQuantity;
    private Integer minOrderQuantity;
    private Integer maxOrderQuantity;
    private boolean inStock;
    private boolean featured;
    private boolean active;
    private String categoryId;
    
    @Column("attributes")
    private List<Attribute> attributes;
    
    @Column("specifications")
    private Map<String, String> specifications;
    
    private Double weight;
    private String weightUnit;
    private String dimensions;
    
    @Column("tags")
    private List<String> tags;
    
    private Integer viewCount;
    private Integer orderCount;
    private Double averageRating;
    private Integer reviewCount;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    
    private String createdAt;
    private String updatedAt;
    private String createdBy;
    private String updatedBy;
}
