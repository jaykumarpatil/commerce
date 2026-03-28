package com.projects.microservices.core.catalog.outbound.client;

import com.projects.api.core.recommendation.Recommendation;
import com.projects.microservices.core.catalog.outbound.port.RecommendationReadPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
public class RecommendationServiceClient implements RecommendationReadPort {

    private static final ParameterizedTypeReference<List<Recommendation>> RECOMMENDATION_LIST = new ParameterizedTypeReference<>() {};

    private final WebClient webClient;

    public RecommendationServiceClient(WebClient.Builder webClientBuilder,
                                       @Value("${app.clients.recommendation-service.base-url:http://recommendation-service}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public Flux<Recommendation> getRecommendationsByProductId(String productId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/recommendation")
                        .queryParam("productId", productId)
                        .build())
                .retrieve()
                .bodyToMono(RECOMMENDATION_LIST)
                .flatMapMany(Flux::fromIterable);
    }
}
