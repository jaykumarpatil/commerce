package com.projects.microservices.cart.adapter.outbound.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import com.projects.microservices.cart.domain.model.CartEntity;

@Repository
public interface CartMongoRepository extends ReactiveMongoRepository<CartEntity, String> {
    Mono<CartEntity> findByCartId(String cartId);
    Mono<CartEntity> findByUserId(String userId);
}
