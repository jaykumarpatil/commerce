package com.projects.microservices.catalog.application.port.inbound;

import com.projects.api.core.product.Product;
import reactor.core.publisher.Mono;

public interface CatalogCommandPort {
    Mono<Product> createProduct(Product product);
    Mono<Product> updateProduct(String productId, Product product);
    Mono<Void> deleteProduct(String productId);
}
