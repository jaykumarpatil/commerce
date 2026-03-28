package com.projects.microservices.catalog.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class ProductEntity {
    @Id
    private String id;
    private String productId;
    private String name;
    private String description;
    private Double price;
    private Integer stockQuantity;
    private String categoryId;
    private String brand;
    private String imageUrl;
    private boolean active;
    private java.util.List<String> tags;
    private java.util.Map<String, String> attributes;
}
