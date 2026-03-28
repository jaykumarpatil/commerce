package com.projects.microservices.cart.application.usecase;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.projects.api.core.cart.Cart;
import com.projects.api.exceptions.NotFoundException;
import com.projects.microservices.cart.application.port.inbound.CartQueryPort;
import com.projects.microservices.cart.application.port.outbound.CartCachePort;
import com.projects.microservices.cart.application.port.outbound.CartRepositoryPort;
import com.projects.microservices.cart.domain.model.CartEntity;
import com.projects.microservices.cart.domain.model.CartItemEntity;
import com.projects.microservices.cart.domain.service.CartDomainService;

@Service
public class CartQueryUseCase implements CartQueryPort {

    private final CartRepositoryPort repository;
    private final CartCachePort cache;
    private final CartDomainService domainService;

    public CartQueryUseCase(CartRepositoryPort repository, CartCachePort cache, CartDomainService domainService) {
        this.repository = repository;
        this.cache = cache;
        this.domainService = domainService;
    }

    @Override
    public Mono<Cart> getCart(String cartId) {
        return cache.get(cartId)
                .switchIfEmpty(repository.findByCartId(cartId)
                        .flatMap(cart -> cache.set(cartId, cart).thenReturn(cart)))
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .map(this::mapToApi);
    }

    @Override
    public Mono<Cart> getCartByUserId(String userId) {
        return repository.findByUserId(userId)
                .map(this::mapToApi)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found for user: " + userId)));
    }

    @Override
    public Mono<Cart> calculateTotals(String cartId) {
        return repository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(domainService::calculateTotals)
                .flatMap(repository::save)
                .flatMap(cart -> cache.set(cartId, cart).thenReturn(cart))
                .map(this::mapToApi);
    }

    private Cart mapToApi(CartEntity entity) {
        if (entity == null) return null;
        
        Cart cart = new Cart();
        cart.setCartId(entity.getCartId());
        cart.setUserId(entity.getUserId());
        cart.setSubtotal(entity.getSubtotal());
        cart.setDiscountTotal(entity.getDiscountTotal());
        cart.setTaxAmount(entity.getTaxAmount());
        cart.setShippingCost(entity.getShippingCost());
        cart.setGrandTotal(entity.getGrandTotal());
        cart.setItemTotalCount(entity.getItemTotalCount());
        cart.setCreatedAt(entity.getCreatedAt());
        cart.setUpdatedAt(entity.getUpdatedAt());
        cart.setItems(entity.getItems() != null ? 
                entity.getItems().stream().map(this::mapItemToApi).toList() : java.util.List.of());
        return cart;
    }

    private com.projects.api.core.cart.CartItem mapItemToApi(CartItemEntity entity) {
        com.projects.api.core.cart.CartItem item = new com.projects.api.core.cart.CartItem();
        item.setCartItemId(entity.getCartItemId());
        item.setProductId(entity.getProductId());
        item.setProductName(entity.getProductName());
        item.setProductImage(entity.getProductImage());
        item.setUnitPrice(entity.getUnitPrice());
        item.setQuantity(entity.getQuantity());
        item.setMaxOrderQuantity(entity.getMaxOrderQuantity());
        item.setInStock(entity.isInStock());
        item.setDiscountAmount(entity.getDiscountAmount());
        return item;
    }
}
