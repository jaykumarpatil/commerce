package com.projects.microservices.core.order.service.port.outbound;

import com.projects.api.core.cart.Cart;
import reactor.core.publisher.Mono;

public interface ShoppingCartPort {
    Mono<Cart> getCart(String cartId);
    Mono<Void> clearCart(String cartId);
}
