package com.projects.microservices.core.catalog.outbound.port;

import com.projects.api.core.recommendation.Recommendation;
import reactor.core.publisher.Flux;

public interface RecommendationReadPort {
    Flux<Recommendation> getRecommendationsByProductId(String productId);
}
