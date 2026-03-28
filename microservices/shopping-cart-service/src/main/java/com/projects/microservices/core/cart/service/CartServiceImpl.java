package com.projects.microservices.core.cart.service;

import static java.util.logging.Level.FINE;

import com.projects.api.core.cart.Cart;
import com.projects.api.core.cart.CartItem;
import com.projects.api.core.cart.CartService;
import com.projects.api.core.cart.CartValidationResult;
import com.projects.api.exceptions.BadRequestException;
import com.projects.api.exceptions.InvalidInputException;
import com.projects.api.exceptions.NotFoundException;
import com.projects.microservices.core.cart.persistence.CartEntity;
import com.projects.microservices.core.cart.persistence.CartItemEntity;
import com.projects.microservices.core.cart.persistence.CartItemRepository;
import com.projects.microservices.core.cart.persistence.CartItemOptionRepository;
import com.projects.microservices.core.cart.persistence.CartRepository;
import com.projects.microservices.core.cart.service.client.CatalogServiceClient;
import com.projects.microservices.core.cart.service.client.InventoryServiceClient;
import com.projects.microservices.core.cart.service.client.UserServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger LOG = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemOptionRepository cartItemOptionRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final CartItemOptionMapper cartItemOptionMapper;
    private final CatalogServiceClient catalogServiceClient;
    private final InventoryServiceClient inventoryServiceClient;
    private final UserServiceClient userServiceClient;

    @Value("${cart.item.max-quantity:20}")
    private int maxItemQuantity;

    @Value("${cart.expiration.days:30}")
    private int cartExpirationDays;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           CartItemOptionRepository cartItemOptionRepository,
                           CartMapper cartMapper,
                           CartItemMapper cartItemMapper,
                           CartItemOptionMapper cartItemOptionMapper,
                           CatalogServiceClient catalogServiceClient,
                           InventoryServiceClient inventoryServiceClient,
                           UserServiceClient userServiceClient) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartItemOptionRepository = cartItemOptionRepository;
        this.cartMapper = cartMapper;
        this.cartItemMapper = cartItemMapper;
        this.cartItemOptionMapper = cartItemOptionMapper;
        this.catalogServiceClient = catalogServiceClient;
        this.inventoryServiceClient = inventoryServiceClient;
        this.userServiceClient = userServiceClient;
    }

    @Override
    public Mono<Cart> createCart(Cart cart) {
        if (cart.getUserId() == null || cart.getUserId().isEmpty()) {
            return Mono.error(new InvalidInputException("User ID is required"));
        }

        return validateUserExists(cart.getUserId())
                .then(Mono.defer(() -> {
                    CartEntity entity = cartMapper.apiToEntity(cart);
                    String now = ZonedDateTime.now().toString();
                    entity.setCreatedAt(now);
                    entity.setUpdatedAt(now);
                    if (entity.getItems() == null) {
                        entity.setItems(new ArrayList<>());
                    }
                    return cartRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .onErrorMap(DuplicateKeyException.class,
                                    ex -> new InvalidInputException("Duplicate cart ID: " + cart.getCartId()))
                            .map(cartMapper::entityToApi);
                }));
    }

    @Override
    public Mono<Cart> getCart(String cartId) {
        return cartRepository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(this::clearIfExpired)
                .map(cartMapper::entityToApi);
    }

    @Override
    public Mono<Cart> getCartByUserId(String userId) {
        return validateUserExists(userId)
                .then(cartRepository.findByUserId(userId)
                        .flatMap(this::clearIfExpired)
                        .switchIfEmpty(Mono.defer(() -> {
                            Cart newCart = new Cart();
                            newCart.setUserId(userId);
                            String now = ZonedDateTime.now().toString();
                            CartEntity entity = cartMapper.apiToEntity(newCart);
                            entity.setCreatedAt(now);
                            entity.setUpdatedAt(now);
                            entity.setItems(new ArrayList<>());
                            return cartRepository.save(entity);
                        }))
                        .map(cartMapper::entityToApi));
    }

    @Override
    public Mono<Cart> addItem(String cartId, CartItem item) {
        return cartRepository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(this::clearIfExpired)
                .flatMap(cartEntity -> validateItemRequest(cartEntity.getUserId(), item)
                        .flatMap(product -> inventoryServiceClient.getInventoryByProductId(item.getProductId())
                                .flatMap(inventory -> {
                                    validateQuantity(item.getQuantity(), item.getProductId());
                                    validateQuantityAvailable(item.getProductId(), item.getQuantity(), inventory.getAvailableQuantity());

                                    boolean itemExists = cartEntity.getItems().stream()
                                            .anyMatch(i -> i.getProductId().equals(item.getProductId()));
                                    if (itemExists) {
                                        return Mono.error(new BadRequestException("Item already exists in cart"));
                                    }

                                    CartItemEntity itemEntity = cartItemMapper.apiToEntity(item);
                                    itemEntity.setCartItemId(java.util.UUID.randomUUID().toString());
                                    applyCatalogSnapshot(itemEntity, product, item.getQuantity());

                                    cartEntity.getItems().add(itemEntity);
                                    return calculateTotals(cartId, cartEntity)
                                            .flatMap(cartRepository::save)
                                            .map(cartMapper::entityToApi);
                                })));
    }

    @Override
    public Mono<Cart> updateItem(String cartId, String itemId, CartItem item) {
        return cartRepository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(this::clearIfExpired)
                .flatMap(cartEntity -> {
                    int itemIndex = findItemIndex(cartEntity, itemId);
                    if (itemIndex == -1) {
                        return Mono.error(new NotFoundException("Item not found in cart: " + itemId));
                    }

                    return validateItemRequest(cartEntity.getUserId(), item)
                            .flatMap(product -> inventoryServiceClient.getInventoryByProductId(item.getProductId())
                                    .flatMap(inventory -> {
                                        validateQuantity(item.getQuantity(), item.getProductId());
                                        validateQuantityAvailable(item.getProductId(), item.getQuantity(), inventory.getAvailableQuantity());

                                        CartItemEntity itemEntity = cartItemMapper.apiToEntity(item);
                                        itemEntity.setCartItemId(itemId);
                                        applyCatalogSnapshot(itemEntity, product, item.getQuantity());

                                        cartEntity.getItems().set(itemIndex, itemEntity);
                                        return calculateTotals(cartId, cartEntity)
                                                .flatMap(cartRepository::save)
                                                .map(cartMapper::entityToApi);
                                    }));
                });
    }

    @Override
    public Mono<Cart> removeItem(String cartId, String itemId) {
        return cartRepository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(this::clearIfExpired)
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
                .flatMap(this::clearIfExpired)
                .flatMap(cartEntity -> calculateTotals(cartId, cartEntity)
                        .flatMap(cartRepository::save)
                        .map(cartMapper::entityToApi));
    }

    @Override
    public Mono<CartValidationResult> validateCart(String cartId) {
        return cartRepository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(this::clearIfExpired)
                .flatMap(cart -> {
                    List<String> issues = new ArrayList<>();
                    List<CartItemEntity> items = cart.getItems() == null ? List.of() : cart.getItems();
                    return reactor.core.publisher.Flux.fromIterable(items)
                            .flatMap(item -> inventoryServiceClient.getInventoryByProductId(item.getProductId())
                                    .map(inventory -> {
                                        int available = Objects.requireNonNullElse(inventory.getAvailableQuantity(), 0);
                                        if (available < Objects.requireNonNullElse(item.getQuantity(), 0)) {
                                            issues.add("Stock drift detected for product " + item.getProductId() +
                                                    ": requested=" + item.getQuantity() + ", available=" + available);
                                        }
                                        return item;
                                    }))
                            .then(Mono.just(new CartValidationResult(
                                    cartId,
                                    issues.isEmpty(),
                                    issues,
                                    ZonedDateTime.now().toString()
                            )));
                });
    }

    public Mono<Long> clearExpiredCarts() {
        return cartRepository.findAll()
                .filter(this::isExpired)
                .flatMap(cartRepository::delete)
                .count()
                .doOnNext(count -> {
                    if (count > 0) {
                        LOG.info("Expired cart cleanup removed {} carts", count);
                    }
                });
    }

    private Mono<com.projects.api.core.catalog.Product> validateItemRequest(String userId, CartItem item) {
        if (item == null || item.getProductId() == null || item.getProductId().isBlank()) {
            return Mono.error(new InvalidInputException("productId is required"));
        }

        return validateUserExists(userId)
                .then(catalogServiceClient.getProduct(item.getProductId()))
                .flatMap(product -> {
                    if (!product.isActive() || !product.isInStock()) {
                        return Mono.error(new BadRequestException("Product is not sellable: " + item.getProductId()));
                    }
                    return Mono.just(product);
                });
    }

    private Mono<Void> validateUserExists(String userId) {
        if (userId == null || userId.isBlank()) {
            return Mono.error(new InvalidInputException("User ID is required"));
        }
        return userServiceClient.getUser(userId).then();
    }

    private void validateQuantity(Integer quantity, String productId) {
        int qty = Objects.requireNonNullElse(quantity, 0);
        if (qty < 1) {
            throw new BadRequestException("Minimum quantity is 1 for product: " + productId);
        }
        if (qty > maxItemQuantity) {
            throw new BadRequestException("Requested quantity exceeds max allowed " + maxItemQuantity + " for product: " + productId);
        }
    }

    private void validateQuantityAvailable(String productId, Integer quantity, Integer availableQuantity) {
        int requested = Objects.requireNonNullElse(quantity, 0);
        int available = Objects.requireNonNullElse(availableQuantity, 0);
        if (requested > available) {
            throw new BadRequestException("Insufficient stock for product " + productId + ", requested=" + requested + ", available=" + available);
        }
    }

    private void applyCatalogSnapshot(CartItemEntity itemEntity, com.projects.api.core.catalog.Product product, Integer quantity) {
        itemEntity.setProductId(product.getProductId());
        itemEntity.setProductName(product.getName());
        itemEntity.setProductImage(product.getMainImage());
        itemEntity.setUnitPrice(product.getPrice());
        itemEntity.setQuantity(quantity);
        itemEntity.setMaxOrderQuantity(Math.min(maxItemQuantity, Objects.requireNonNullElse(product.getMaxOrderQuantity(), maxItemQuantity)));
        itemEntity.setInStock(product.isInStock());
        itemEntity.setCreatedAt(ZonedDateTime.now().toString());
    }

    private Mono<CartEntity> calculateTotals(String cartId, CartEntity cartEntity) {
        double subtotal = 0;
        int itemCount = 0;

        for (CartItemEntity item : cartEntity.getItems()) {
            double itemTotal = item.getUnitPrice() * item.getQuantity();
            subtotal += itemTotal;
            itemCount += item.getQuantity();
        }

        double taxAmount = subtotal * 0.1;
        double shippingCost = subtotal > 50 ? 0 : 9.99;
        double grandTotal = subtotal + taxAmount + shippingCost;

        cartEntity.setSubtotal(subtotal);
        cartEntity.setTaxAmount(taxAmount);
        cartEntity.setShippingCost(shippingCost);
        cartEntity.setGrandTotal(grandTotal);
        cartEntity.setItemTotalCount(itemCount);
        cartEntity.setUpdatedAt(ZonedDateTime.now().toString());

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
            mergedQty = Math.min(mergedQty, maxItemQuantity);
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

    private Mono<CartEntity> clearIfExpired(CartEntity cartEntity) {
        if (!isExpired(cartEntity)) {
            return Mono.just(cartEntity);
        }
        cartEntity.setItems(new ArrayList<>());
        cartEntity.setSubtotal(0d);
        cartEntity.setDiscountTotal(0d);
        cartEntity.setTaxAmount(0d);
        cartEntity.setShippingCost(0d);
        cartEntity.setGrandTotal(0d);
        cartEntity.setItemTotalCount(0);
        cartEntity.setUpdatedAt(ZonedDateTime.now().toString());
        return cartRepository.save(cartEntity);
    }

    private boolean isExpired(CartEntity cartEntity) {
        String activityTime = cartEntity.getUpdatedAt() != null ? cartEntity.getUpdatedAt() : cartEntity.getCreatedAt();
        if (activityTime == null) {
            return false;
        }
        try {
            ZonedDateTime lastActivity = ZonedDateTime.parse(activityTime);
            return lastActivity.isBefore(ZonedDateTime.now().minus(cartExpirationDays, ChronoUnit.DAYS));
        } catch (Exception ex) {
            LOG.warn("Unable to parse cart timestamp '{}', skipping expiration check", activityTime);
            return false;
        }
    }
}
