package com.projects.microservices.cart.application.port.outbound;

import com.projects.microservices.cart.domain.model.CartEntity;
import reactor.core.publisher.Mono;

public interface CartCachePort {
    Mono<CartEntity> get(String cartId);
    Mono<Void> set(String cartId, CartEntity cart);
    Mono<Void> evict(String cartId);
    Mono<Boolean> exists(String cartId);
}
