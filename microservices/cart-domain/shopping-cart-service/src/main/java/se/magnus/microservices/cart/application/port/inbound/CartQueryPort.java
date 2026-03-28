package se.magnus.microservices.cart.application.port.inbound;

import se.magnus.api.core.cart.Cart;
import reactor.core.publisher.Mono;

public interface CartQueryPort {
    Mono<Cart> getCart(String cartId);
    Mono<Cart> getCartByUserId(String userId);
    Mono<Cart> calculateTotals(String cartId);
}
