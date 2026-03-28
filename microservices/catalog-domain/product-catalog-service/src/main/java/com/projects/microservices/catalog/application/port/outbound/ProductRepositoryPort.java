package com.projects.microservices.catalog.application.port.outbound;

import com.projects.microservices.catalog.domain.model.ProductEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepositoryPort {
    Mono<ProductEntity> save(ProductEntity product);
    Mono<ProductEntity> findByProductId(String productId);
    Flux<ProductEntity> findByCategoryId(String categoryId);
    Flux<ProductEntity> searchByName(String name);
    Mono<Void> delete(String productId);
}
