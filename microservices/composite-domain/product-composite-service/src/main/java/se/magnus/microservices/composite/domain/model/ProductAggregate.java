package se.magnus.microservices.composite.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAggregate {
    private Product product;
    private java.util.List<Recommendation> recommendations;
    private java.util.List<Review> reviews;
}
