package com.projects.microservices.composite.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.projects.api.core.product.Product;
import com.projects.api.core.recommendation.Recommendation;
import com.projects.api.core.review.Review;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAggregate {
    private Product product;
    private java.util.List<Recommendation> recommendations;
    private java.util.List<Review> reviews;
}
