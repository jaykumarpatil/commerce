package se.magnus.microservices.cart.application.port.inbound;

import reactor.core.publisher.Mono;
import se.magnus.api.core.cart.Cart;
import se.magnus.api.core.cart.CartItem;

public interface CartCommandPort {
    Mono<Cart> createCart(String userId);
    Mono<Cart> addItem(String cartId, CartItem item);
    Mono<Cart> updateItem(String cartId, String itemId, CartItem item);
    Mono<Cart> removeItem(String cartId, String itemId);
    Mono<Void> deleteCart(String cartId);
    Mono<Void> deleteCartByUserId(String userId);
    Mono<Cart> mergeCart(String cartId, String sessionId);
}
