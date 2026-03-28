package com.projects.microservices.core.catalog.outbound.port;

import com.projects.api.core.catalog.Category;
import com.projects.api.core.catalog.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductReadPort {
    Flux<Product> getAllProducts(int page, int size);
    Mono<Product> getProduct(String productId);
    Flux<Product> searchProducts(String query, int page, int size);
    Flux<Product> getProductsByCategory(String categoryId, int page, int size);
    Flux<Category> getAllCategories();
    Mono<Category> getCategory(String categoryId);
}
