package com.projects.microservices.core.catalog.outbound.client;

import com.projects.api.core.review.Review;
import com.projects.microservices.core.catalog.outbound.port.ReviewReadPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
public class ReviewServiceClient implements ReviewReadPort {

    private static final ParameterizedTypeReference<List<Review>> REVIEW_LIST = new ParameterizedTypeReference<>() {};

    private final WebClient webClient;

    public ReviewServiceClient(WebClient.Builder webClientBuilder,
                               @Value("${app.clients.review-service.base-url:http://review-service}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public Flux<Review> getReviewsByProductId(String productId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/review")
                        .queryParam("productId", productId)
                        .build())
                .retrieve()
                .bodyToMono(REVIEW_LIST)
                .flatMapMany(Flux::fromIterable);
    }
}
