package com.projects.microservices.core.cart.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CartItemRepository extends ReactiveMongoRepository<CartItemEntity, String> {
    Mono<CartItemEntity> findByCartItemId(String cartItemId);
}
