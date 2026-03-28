package com.projects.microservices.core.cart.service.client;

import com.projects.api.core.catalog.Product;
import com.projects.api.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class CatalogServiceClient {

    private static final String CATALOG_SERVICE_URL = "http://product-catalog-service";

    private final WebClient webClient;

    @Autowired
    public CatalogServiceClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Product> getProduct(String productId) {
        return webClient.get()
                .uri(CATALOG_SERVICE_URL + "/v1/products/{productId}", productId)
                .retrieve()
                .bodyToMono(Product.class)
                .onErrorMap(WebClientResponseException.NotFound.class,
                        ex -> new NotFoundException("Product not found: " + productId));
    }
}
