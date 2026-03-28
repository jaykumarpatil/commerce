package com.projects.api.core.cart;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartService {

    @PostMapping("/v1/carts")
    Mono<Cart> createCart(Cart cart);

    @GetMapping("/v1/carts/{cartId}")
    Mono<Cart> getCart(String cartId);

    @GetMapping("/v1/carts/user/{userId}")
    Mono<Cart> getCartByUserId(String userId);

    @PostMapping("/v1/carts/{cartId}/items")
    Mono<Cart> addItem(String cartId, CartItem item);

    @PutMapping("/v1/carts/{cartId}/items/{itemId}")
    Mono<Cart> updateItem(String cartId, String itemId, CartItem item);

    @DeleteMapping("/v1/carts/{cartId}/items/{itemId}")
    Mono<Cart> removeItem(String cartId, String itemId);

    @DeleteMapping("/v1/carts/{cartId}")
    Mono<Void> deleteCart(String cartId);

    @DeleteMapping("/v1/carts/user/{userId}")
    Mono<Void> deleteCartByUserId(String userId);

    @PostMapping("/v1/carts/{cartId}/merge")
    Mono<Cart> mergeCart(String cartId, String sessionId);

    @PostMapping("/v1/carts/{cartId}/calculate")
    Mono<Cart> calculateTotals(String cartId);
}
