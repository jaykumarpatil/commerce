package com.projects.microservices.core.catalog.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("variants")
public class VariantEntity {
    @Id
    private Long id;
    private String variantId;
    private String productId;
    private String name;
    private String sku;
    private Double price;
    private Integer stockQuantity;
    private boolean active;
    private String attributesJson;
    private String createdAt;
    private String updatedAt;
}
