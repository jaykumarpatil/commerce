package com.projects.microservices.core.cart.service;

import static java.util.logging.Level.FINE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.projects.api.core.cart.*;
import com.projects.api.exceptions.BadRequestException;
import com.projects.api.exceptions.InvalidInputException;
import com.projects.api.exceptions.NotFoundException;
import com.projects.microservices.core.cart.persistence.*;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger LOG = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemOptionRepository cartItemOptionRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final CartItemOptionMapper cartItemOptionMapper;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           CartItemOptionRepository cartItemOptionRepository,
                           CartMapper cartMapper,
                           CartItemMapper cartItemMapper,
                           CartItemOptionMapper cartItemOptionMapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartItemOptionRepository = cartItemOptionRepository;
        this.cartMapper = cartMapper;
        this.cartItemMapper = cartItemMapper;
        this.cartItemOptionMapper = cartItemOptionMapper;
    }

    @Override
    public Mono<Cart> createCart(Cart cart) {
        if (cart.getUserId() == null || cart.getUserId().isEmpty()) {
            return Mono.error(new InvalidInputException("User ID is required"));
        }

        CartEntity entity = cartMapper.apiToEntity(cart);
        
        return cartRepository.save(entity)
                .log(LOG.getName(), FINE)
                .onErrorMap(DuplicateKeyException.class, 
                        ex -> new InvalidInputException("Duplicate cart ID: " + cart.getCartId()))
                .map(cartMapper::entityToApi);
    }

    @Override
    public Mono<Cart> getCart(String cartId) {
        return cartRepository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .map(cartMapper::entityToApi);
    }

    @Override
    public Mono<Cart> getCartByUserId(String userId) {
        return cartRepository.findByUserId(userId)
                .<CartEntity>switchIfEmpty(Mono.defer(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(cartMapper.apiToEntity(newCart));
                }))
                .map(cartMapper::entityToApi);
    }

    @Override
    public Mono<Cart> addItem(String cartId, CartItem item) {
        return cartRepository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cartEntity -> {
                    boolean itemExists = cartEntity.getItems().stream()
                            .anyMatch(i -> i.getProductId().equals(item.getProductId()));
                    
                    if (itemExists) {
                        return Mono.error(new BadRequestException("Item already exists in cart"));
                    }
                    
                    CartItemEntity itemEntity = cartItemMapper.apiToEntity(item);
                    itemEntity.setCartItemId(java.util.UUID.randomUUID().toString());
                    
                    cartEntity.getItems().add(itemEntity);
                    return calculateTotals(cartId, cartEntity)
                            .flatMap(cartRepository::save)
                            .map(cartMapper::entityToApi);
                });
    }

    @Override
    public Mono<Cart> updateItem(String cartId, String itemId, CartItem item) {
        return cartRepository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cartEntity -> {
                    int itemIndex = findItemIndex(cartEntity, itemId);
                    if (itemIndex == -1) {
                        return Mono.error(new NotFoundException("Item not found in cart: " + itemId));
                    }
                    
                    CartItemEntity itemEntity = cartItemMapper.apiToEntity(item);
                    itemEntity.setCartItemId(itemId);
                    
                    cartEntity.getItems().set(itemIndex, itemEntity);
                    return calculateTotals(cartId, cartEntity)
                            .flatMap(cartRepository::save)
                            .map(cartMapper::entityToApi);
                });
    }

    @Override
    public Mono<Cart> removeItem(String cartId, String itemId) {
        return cartRepository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cartEntity -> {
                    int itemIndex = findItemIndex(cartEntity, itemId);
                    if (itemIndex == -1) {
                        return Mono.error(new NotFoundException("Item not found in cart: " + itemId));
                    }
                    
                    cartEntity.getItems().remove(itemIndex);
                    return calculateTotals(cartId, cartEntity)
                            .flatMap(cartRepository::save)
                            .map(cartMapper::entityToApi);
                });
    }

    @Override
    public Mono<Void> deleteCart(String cartId) {
        return cartRepository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cartRepository::delete)
                .then(Mono.empty());
    }

    @Override
    public Mono<Void> deleteCartByUserId(String userId) {
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.empty())
                .flatMap(cartRepository::delete)
                .then(Mono.empty());
    }

    @Override
    public Mono<Cart> mergeCart(String cartId, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Mono.error(new InvalidInputException("Session ID is required for cart merge"));
        }

        if (sessionId.equals(cartId)) {
            return calculateTotals(cartId);
        }

        LOG.info("Merging persistent cart {} with guest/session cart {}", cartId, sessionId);

        return cartRepository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .zipWith(cartRepository.findByCartId(sessionId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Guest cart not found: " + sessionId))))
                .flatMap(tuple -> {
                    CartEntity userCart = tuple.getT1();
                    CartEntity guestCart = tuple.getT2();

                    List<CartItemEntity> mergedItems = mergeItems(userCart.getItems(), guestCart.getItems());
                    userCart.setItems(mergedItems);

                    return calculateTotals(cartId, userCart)
                            .flatMap(cartRepository::save)
                            .flatMap(savedUserCart -> cartRepository.delete(guestCart)
                                    .thenReturn(savedUserCart));
                })
                .map(cartMapper::entityToApi);
    }

    @Override
    public Mono<Cart> calculateTotals(String cartId) {
        return cartRepository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cartEntity -> calculateTotals(cartId, cartEntity)
                        .flatMap(cartRepository::save)
                        .map(cartMapper::entityToApi));
    }

    private Mono<CartEntity> calculateTotals(String cartId, CartEntity cartEntity) {
        double subtotal = 0;
        int itemCount = 0;

        for (CartItemEntity item : cartEntity.getItems()) {
            double itemTotal = item.getUnitPrice() * item.getQuantity();
            subtotal += itemTotal;
            itemCount += item.getQuantity();
        }

        double taxAmount = subtotal * 0.1; // 10% tax
        double shippingCost = subtotal > 50 ? 0 : 9.99; // Free shipping over $50
        double grandTotal = subtotal + taxAmount + shippingCost;

        cartEntity.setSubtotal(subtotal);
        cartEntity.setTaxAmount(taxAmount);
        cartEntity.setShippingCost(shippingCost);
        cartEntity.setGrandTotal(grandTotal);
        cartEntity.setItemTotalCount(itemCount);
        cartEntity.setUpdatedAt(java.time.ZonedDateTime.now().toString());

        return Mono.just(cartEntity);
    }

    private int findItemIndex(CartEntity cartEntity, String itemId) {
        for (int i = 0; i < cartEntity.getItems().size(); i++) {
            if (cartEntity.getItems().get(i).getCartItemId().equals(itemId)) {
                return i;
            }
        }
        return -1;
    }

    private List<CartItemEntity> mergeItems(List<CartItemEntity> targetItems, List<CartItemEntity> sourceItems) {
        Map<String, CartItemEntity> mergedByProduct = new HashMap<>();

        List<CartItemEntity> safeTargetItems = targetItems == null ? List.of() : targetItems;
        List<CartItemEntity> safeSourceItems = sourceItems == null ? List.of() : sourceItems;

        safeTargetItems.forEach(item -> mergedByProduct.put(buildMergeKey(item), cloneWithQuantity(item, item.getQuantity())));

        for (CartItemEntity sourceItem : safeSourceItems) {
            String mergeKey = buildMergeKey(sourceItem);
            CartItemEntity existing = mergedByProduct.get(mergeKey);
            if (existing == null) {
                mergedByProduct.put(mergeKey, cloneWithQuantity(sourceItem, sourceItem.getQuantity()));
                continue;
            }

            int mergedQty = Objects.requireNonNullElse(existing.getQuantity(), 0)
                    + Objects.requireNonNullElse(sourceItem.getQuantity(), 0);
            if (existing.getMaxOrderQuantity() != null && existing.getMaxOrderQuantity() > 0) {
                mergedQty = Math.min(mergedQty, existing.getMaxOrderQuantity());
            }
            existing.setQuantity(mergedQty);
        }

        return new ArrayList<>(mergedByProduct.values());
    }

    private CartItemEntity cloneWithQuantity(CartItemEntity source, Integer quantity) {
        CartItemEntity clone = cartItemMapper.apiToEntity(cartItemMapper.entityToApi(source));
        clone.setQuantity(Objects.requireNonNullElse(quantity, 0));
        return clone;
    }

    private String buildMergeKey(CartItemEntity item) {
        return item.getProductId() + "::" + Objects.requireNonNullElse(item.getUnitPrice(), 0d);
    }
}
