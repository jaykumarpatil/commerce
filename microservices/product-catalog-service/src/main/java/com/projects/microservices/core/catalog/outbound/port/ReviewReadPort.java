package com.projects.microservices.core.catalog.outbound.port;

import com.projects.api.core.review.Review;
import reactor.core.publisher.Flux;

public interface ReviewReadPort {
    Flux<Review> getReviewsByProductId(String productId);
}
