package com.projects.microservices.core.cart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.projects.api.core.cart.*;
import com.projects.util.http.ServiceUtil;

@RestController
public class CartController implements CartService {

    private final CartService cartService;
    private final ServiceUtil serviceUtil;

    @Autowired
    public CartController(CartService cartService, ServiceUtil serviceUtil) {
        this.cartService = cartService;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Cart> createCart(Cart cart) {
        return cartService.createCart(cart);
    }

    @Override
    public Mono<Cart> getCart(String cartId) {
        return cartService.getCart(cartId);
    }

    @Override
    public Mono<Cart> getCartByUserId(String userId) {
        return cartService.getCartByUserId(userId);
    }

    @Override
    public Mono<Cart> addItem(String cartId, CartItem item) {
        return cartService.addItem(cartId, item);
    }

    @Override
    public Mono<Cart> updateItem(String cartId, String itemId, CartItem item) {
        return cartService.updateItem(cartId, itemId, item);
    }

    @Override
    public Mono<Cart> removeItem(String cartId, String itemId) {
        return cartService.removeItem(cartId, itemId);
    }

    @Override
    public Mono<Void> deleteCart(String cartId) {
        return cartService.deleteCart(cartId);
    }

    @Override
    public Mono<Void> deleteCartByUserId(String userId) {
        return cartService.deleteCartByUserId(userId);
    }

    @Override
    public Mono<Cart> mergeCart(String cartId, String sessionId) {
        return cartService.mergeCart(cartId, sessionId);
    }

    @Override
    public Mono<Cart> calculateTotals(String cartId) {
        return cartService.calculateTotals(cartId);
    }

    @Override
    public Mono<CartValidationResult> validateCart(String cartId) {
        return cartService.validateCart(cartId);
    }
}
