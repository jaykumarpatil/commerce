package se.magnus.microservices.core.cart.service;

import static java.util.logging.Level.FINE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import se.magnus.api.core.cart.*;
import se.magnus.api.exceptions.BadRequestException;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.microservices.core.cart.persistence.*;

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
                .switchIfEmpty(Mono.defer(() -> {
                    // Create new cart for user if none exists
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return createCart(newCart);
                }))
                .map(cartMapper::entityToApi);
    }

    @Override
    public Mono<Cart> addItem(String cartId, CartItem item) {
        return cartRepository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(cartEntity -> {
                    // Check if item already exists
                    boolean itemExists = cartEntity.getItems().stream()
                            .anyMatch(i -> i.getProductId().equals(item.getProductId()));
                    
                    if (itemExists) {
                        // Update quantity
                        return Mono.error(new BadRequestException("Item already exists in cart"));
                    }
                    
                    CartItemEntity itemEntity = cartItemMapper.apiToEntity(item);
                    itemEntity.setCartItemId(java.util.UUID.randomUUID().toString());
                    
                    cartEntity.getItems().add(itemEntity);
                    return calculateTotals(cartId, cartEntity);
                })
                .flatMap(cartRepository::save)
                .log(LOG.getName(), FINE)
                .map(cartMapper::entityToApi);
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
                    return calculateTotals(cartId, cartEntity);
                })
                .flatMap(cartRepository::save)
                .log(LOG.getName(), FINE)
                .map(cartMapper::entityToApi);
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
                    return calculateTotals(cartId, cartEntity);
                })
                .flatMap(cartRepository::save)
                .log(LOG.getName(), FINE)
                .map(cartMapper::entityToApi);
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
        // TODO: Implement cart merging logic
        LOG.info("Merging cart {} with session {}", cartId, sessionId);
        return getCart(cartId);
    }

    @Override
    public Mono<Cart> calculateTotals(String cartId) {
        return cartRepository.findByCartId(cartId)
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + cartId)))
                .flatMap(this::calculateTotals);
    }

    private Mono<Cart> calculateTotals(String cartId, CartEntity cartEntity) {
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

        return Mono.just(cartMapper.entityToApi(cartEntity));
    }

    private int findItemIndex(CartEntity cartEntity, String itemId) {
        for (int i = 0; i < cartEntity.getItems().size(); i++) {
            if (cartEntity.getItems().get(i).getCartItemId().equals(itemId)) {
                return i;
            }
        }
        return -1;
    }
}
