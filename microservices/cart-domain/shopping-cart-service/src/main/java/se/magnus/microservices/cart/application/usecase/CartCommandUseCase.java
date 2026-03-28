package se.magnus.microservices.cart.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import se.magnus.api.core.cart.Cart;
import se.magnus.api.core.cart.CartItem;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.microservices.cart.application.port.inbound.CartCommandPort;
import se.magnus.microservices.cart.application.port.inbound.CartQueryPort;
import se.magnus.microservices.cart.application.port.outbound.CartCachePort;
import se.magnus.microservices.cart.application.port.outbound.CartRepositoryPort;
import se.magnus.microservices.cart.domain.model.CartEntity;
import se.magnus.microservices.cart.domain.model.CartItemEntity;
import se.magnus.microservices.cart.domain.service.CartDomainService;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class CartCommandUseCase implements CartCommandPort {

    private static final Logger LOG = LoggerFactory.getLogger(CartCommandUseCase.class);

    private final CartRepositoryPort repository;
    private final CartCachePort cache;
    private final CartDomainService domainService;

    public CartCommandUseCase(CartRepositoryPort repository, CartCachePort cache, CartDomainService domainService) {
        this.repository = repository;
        this.cache = cache;
        this.domainService = domainService;
    }

    @Override
    public Mono<Cart> createCart(String userId) {
        if (userId == null || userId.isBlank()) {
            return Mono.error(new InvalidInputException("User ID is required"));
        }

        CartEntity entity = new CartEntity();
        entity.setCartId(UUID.randomUUID().toString());
        entity.setUserId(userId);
        entity.setCreatedAt(ZonedDateTime.now().toString());

        return repository.save(entity)
                .flatMap(domainService::calculateTotals)
                .flatMap(repository::save)
                .flatMap(this::cacheAndReturn);
    }

    @Override
    public Mono<Cart> addItem(String cartId, CartItem item) {
        return repository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cart -> {
                    boolean exists = cart.getItems().stream()
                            .anyMatch(i -> i.getProductId().equals(item.getProductId()));
                    
                    if (exists) {
                        return Mono.error(new InvalidInputException("Item already exists in cart"));
                    }
                    
                    CartItemEntity itemEntity = mapToEntity(item);
                    itemEntity.setCartItemId(UUID.randomUUID().toString());
                    cart.addItem(itemEntity);
                    
                    return persistCart(cart);
                });
    }

    @Override
    public Mono<Cart> updateItem(String cartId, String itemId, CartItem item) {
        return repository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cart -> {
                    CartItemEntity existing = cart.findItem(itemId);
                    if (existing == null) {
                        return Mono.error(new NotFoundException("Item not found in cart: " + itemId));
                    }
                    
                    CartItemEntity updated = mapToEntity(item);
                    updated.setCartItemId(itemId);
                    cart.updateItem(itemId, updated);
                    
                    return persistCart(cart);
                });
    }

    @Override
    public Mono<Cart> removeItem(String cartId, String itemId) {
        return repository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cart -> {
                    cart.removeItem(itemId);
                    return persistCart(cart);
                });
    }

    @Override
    public Mono<Void> deleteCart(String cartId) {
        return cache.evict(cartId)
                .then(repository.delete(cartId));
    }

    @Override
    public Mono<Void> deleteCartByUserId(String userId) {
        return repository.findByUserId(userId)
                .flatMap(cart -> repository.delete(cart.getCartId()))
                .then();
    }

    @Override
    public Mono<Cart> mergeCart(String cartId, String sessionId) {
        LOG.info("Merging cart {} with session {}", cartId, sessionId);
        return repository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .map(this::mapToApi);
    }

    private Mono<Cart> persistCart(CartEntity cart) {
        return domainService.calculateTotals(cart)
                .flatMap(repository::save)
                .flatMap(this::cacheAndReturn);
    }

    private Mono<Cart> cacheAndReturn(CartEntity entity) {
        return cache.set(entity.getCartId(), entity)
                .thenReturn(mapToApi(entity));
    }

    private CartItemEntity mapToEntity(CartItem item) {
        CartItemEntity entity = new CartItemEntity();
        entity.setProductId(item.getProductId());
        entity.setProductName(item.getProductName());
        entity.setProductImage(item.getProductImage());
        entity.setUnitPrice(item.getUnitPrice());
        entity.setQuantity(item.getQuantity());
        entity.setMaxOrderQuantity(item.getMaxOrderQuantity());
        entity.setInStock(item.isInStock());
        entity.setDiscountAmount(item.getDiscountAmount());
        entity.setCreatedAt(Instant.now().toString());
        return entity;
    }

    private Cart mapToApi(CartEntity entity) {
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
        cart.setItems(entity.getItems().stream().map(this::mapItemToApi).toList());
        return cart;
    }

    private se.magnus.api.core.cart.CartItem mapItemToApi(CartItemEntity entity) {
        se.magnus.api.core.cart.CartItem item = new se.magnus.api.core.cart.CartItem();
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
