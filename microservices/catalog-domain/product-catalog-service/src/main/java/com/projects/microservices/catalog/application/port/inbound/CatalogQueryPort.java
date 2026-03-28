package com.projects.microservices.catalog.application.port.inbound;

import com.projects.api.core.product.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CatalogQueryPort {
    Mono<Product> getProduct(String productId);
    Flux<Product> getProductsByCategory(String categoryId);
    Flux<Product> searchProducts(String query);
}
