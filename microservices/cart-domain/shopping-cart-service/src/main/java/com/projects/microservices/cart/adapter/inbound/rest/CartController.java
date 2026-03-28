package com.projects.microservices.cart.adapter.inbound.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import com.projects.api.core.cart.Cart;
import com.projects.api.core.cart.CartItem;
import com.projects.microservices.cart.application.port.inbound.CartCommandPort;
import com.projects.microservices.cart.application.port.inbound.CartQueryPort;

@RestController
public class CartController {

    private final CartCommandPort commandPort;
    private final CartQueryPort queryPort;

    @Autowired
    public CartController(CartCommandPort commandPort, CartQueryPort queryPort) {
        this.commandPort = commandPort;
        this.queryPort = queryPort;
    }

    public Mono<Cart> createCart(String userId) {
        return commandPort.createCart(userId);
    }

    public Mono<Cart> getCart(String cartId) {
        return queryPort.getCart(cartId);
    }

    public Mono<Cart> getCartByUserId(String userId) {
        return queryPort.getCartByUserId(userId);
    }

    public Mono<Cart> addItem(String cartId, CartItem item) {
        return commandPort.addItem(cartId, item);
    }

    public Mono<Cart> updateItem(String cartId, String itemId, CartItem item) {
        return commandPort.updateItem(cartId, itemId, item);
    }

    public Mono<Cart> removeItem(String cartId, String itemId) {
        return commandPort.removeItem(cartId, itemId);
    }

    public Mono<Void> deleteCart(String cartId) {
        return commandPort.deleteCart(cartId);
    }

    public Mono<Void> deleteCartByUserId(String userId) {
        return commandPort.deleteCartByUserId(userId);
    }

    public Mono<Cart> mergeCart(String cartId, String sessionId) {
        return commandPort.mergeCart(cartId, sessionId);
    }

    public Mono<Cart> calculateTotals(String cartId) {
        return queryPort.calculateTotals(cartId);
    }
}
