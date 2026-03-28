package com.projects.microservices.cart.application.port.outbound;

import com.projects.microservices.cart.domain.model.CartEntity;
import reactor.core.publisher.Mono;

public interface CartRepositoryPort {
    Mono<CartEntity> save(CartEntity cart);
    Mono<CartEntity> findByCartId(String cartId);
    Mono<CartEntity> findByUserId(String userId);
    Mono<Void> delete(String cartId);
    Mono<Void> deleteByUserId(String userId);
}
