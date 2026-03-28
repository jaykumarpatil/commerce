package com.projects.microservices.core.catalog.outbound.client;

import com.projects.api.core.catalog.Category;
import com.projects.api.core.catalog.Product;
import com.projects.microservices.core.catalog.outbound.port.ProductReadPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class ProductServiceClient implements ProductReadPort {

    private static final ParameterizedTypeReference<List<Product>> PRODUCT_LIST = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<Category>> CATEGORY_LIST = new ParameterizedTypeReference<>() {};

    private final WebClient webClient;

    public ProductServiceClient(WebClient.Builder webClientBuilder,
                                @Value("${app.clients.product-service.base-url:http://product-service}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public Flux<Product> getAllProducts(int page, int size) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v1/products")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .bodyToMono(PRODUCT_LIST)
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Product> getProduct(String productId) {
        return webClient.get()
                .uri("/v1/products/{productId}", productId)
                .retrieve()
                .bodyToMono(Product.class);
    }

    @Override
    public Flux<Product> searchProducts(String query, int page, int size) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v1/products/search")
                        .queryParam("query", query)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .bodyToMono(PRODUCT_LIST)
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Flux<Product> getProductsByCategory(String categoryId, int page, int size) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v1/products/category/{categoryId}")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(categoryId))
                .retrieve()
                .bodyToMono(PRODUCT_LIST)
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Flux<Category> getAllCategories() {
        return webClient.get()
                .uri("/v1/categories")
                .retrieve()
                .bodyToMono(CATEGORY_LIST)
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Category> getCategory(String categoryId) {
        return webClient.get()
                .uri("/v1/categories/{categoryId}", categoryId)
                .retrieve()
                .bodyToMono(Category.class);
    }
}
