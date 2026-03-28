package com.projects.microservices.core.cart.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.projects.api.core.cart.Cart;
import com.projects.api.core.cart.CartItem;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.List;

@Service
public class RedisCartService {

    private static final Logger LOG = LoggerFactory.getLogger(RedisCartService.class);
    
    private static final String CART_PREFIX = "cart:";
    private static final String CART_ITEMS_PREFIX = "cart:items:";
    private static final String CART_LOCK_PREFIX = "cart:lock:";
    private static final String SESSION_CART_PREFIX = "session:cart:";
    
    private static final String ADD_ITEM_SCRIPT = """
        local cartKey = KEYS[1]
        local itemKey = KEYS[2]
        local itemJson = ARGV[1]
        local quantity = tonumber(ARGV[2])
        local ttl = tonumber(ARGV[3])
        
        local exists = redis.call('EXISTS', itemKey)
        if exists == 1 then
            local existingQty = tonumber(redis.call('HGET', itemKey, 'quantity') or '0')
            redis.call('HSET', itemKey, 'quantity', existingQty + quantity)
        else
            redis.call('HSET', itemKey, 'data', itemJson)
            redis.call('HSET', itemKey, 'quantity', quantity)
            redis.call('SADD', cartKey, itemKey)
        end
        
        redis.call('EXPIRE', cartKey, ttl)
        
        return 1
        """;
    
    private static final String REMOVE_ITEM_SCRIPT = """
        local cartKey = KEYS[1]
        local itemKey = KEYS[2]
        
        redis.call('SREM', cartKey, itemKey)
        redis.call('DEL', itemKey)
        
        return 1
        """;
    
    private static final String UPDATE_QUANTITY_SCRIPT = """
        local itemKey = KEYS[1]
        local quantity = tonumber(ARGV[1])
        
        if quantity <= 0 then
            redis.call('DEL', itemKey)
            return 0
        end
        
        redis.call('HSET', itemKey, 'quantity', quantity)
        return 1
        """;
    
    private static final String ACQUIRE_LOCK_SCRIPT = """
        local lockKey = KEYS[1]
        local lockValue = ARGV[1]
        local ttl = tonumber(ARGV[2])
        
        if redis.call('EXISTS', lockKey) == 0 then
            redis.call('SET', lockKey, lockValue, 'PX', ttl)
            return 1
        end
        return 0
        """;
    
    private static final String MERGE_CARTS_SCRIPT = """
        local targetCartKey = KEYS[1]
        local sourceCartKey = KEYS[2]
        
        local sourceItems = redis.call('SMEMBERS', sourceCartKey)
        for _, itemKey in ipairs(sourceItems) do
            local itemData = redis.call('HGET', itemKey, 'data')
            local itemQty = tonumber(redis.call('HGET', itemKey, 'quantity') or '0')
            
            local targetItems = redis.call('SMEMBERS', targetCartKey)
            local found = 0
            for _, targetItemKey in ipairs(targetItems) do
                local targetData = redis.call('HGET', targetItemKey, 'data')
                if itemData == targetData then
                    local targetQty = tonumber(redis.call('HGET', targetItemKey, 'quantity') or '0')
                    redis.call('HSET', targetItemKey, 'quantity', targetQty + itemQty)
                    found = 1
                    redis.call('SREM', sourceCartKey, itemKey)
                    redis.call('DEL', itemKey)
                    break
                end
            end
            
            if found == 0 then
                redis.call('SMOVE', sourceCartKey, targetCartKey, itemKey)
            end
        end
        
        return 1
        """;

    private final ReactiveStringRedisTemplate redisTemplate;
    
    @Value("${redis.cart.ttl:2592000}")
    private long cartTtlSeconds;

    private RedisScript<Long> addItemScript;
    private RedisScript<Long> removeItemScript;
    private RedisScript<Long> updateQuantityScript;
    private RedisScript<Long> acquireLockScript;
    private RedisScript<Long> mergeCartsScript;

    public RedisCartService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        addItemScript = RedisScript.of(ADD_ITEM_SCRIPT, Long.class);
        removeItemScript = RedisScript.of(REMOVE_ITEM_SCRIPT, Long.class);
        updateQuantityScript = RedisScript.of(UPDATE_QUANTITY_SCRIPT, Long.class);
        acquireLockScript = RedisScript.of(ACQUIRE_LOCK_SCRIPT, Long.class);
        mergeCartsScript = RedisScript.of(MERGE_CARTS_SCRIPT, Long.class);
    }

    public Mono<Cart> getCart(String cartId) {
        String cartKey = CART_PREFIX + cartId;
        
        return redisTemplate.opsForSet().members(cartKey)
                .flatMap(this::getCartItemFromKey)
                .collectList()
                .map(items -> {
                    Cart cart = new Cart();
                    cart.setCartId(cartId);
                    cart.setItems(new ArrayList<>(items));
                    return cart;
                })
                .switchIfEmpty(Mono.defer(() -> {
                    Cart cart = new Cart();
                    cart.setCartId(cartId);
                    cart.setItems(new ArrayList<>());
                    return Mono.just(cart);
                }));
    }

    public Mono<Cart> addItem(String cartId, CartItem item) {
        String cartKey = CART_PREFIX + cartId;
        String itemKey = CART_ITEMS_PREFIX + cartId + ":" + item.getProductId();
        
        String itemJson = item.getProductId() + "|" + item.getQuantity() + "|" + item.getUnitPrice();
        
        List<String> keys = List.of(cartKey, itemKey);
        List<String> args = List.of(itemJson, String.valueOf(item.getQuantity()), String.valueOf(cartTtlSeconds));
        
        return redisTemplate.execute(addItemScript, keys, args)
                .then(getCart(cartId));
    }

    public Mono<Cart> removeItem(String cartId, String productId) {
        String cartKey = CART_PREFIX + cartId;
        String itemKey = CART_ITEMS_PREFIX + cartId + ":" + productId;
        
        List<String> keys = List.of(cartKey, itemKey);
        
        return redisTemplate.execute(removeItemScript, keys, List.of())
                .then(getCart(cartId));
    }

    public Mono<Cart> updateQuantity(String cartId, String productId, int quantity) {
        String itemKey = CART_ITEMS_PREFIX + cartId + ":" + productId;
        
        List<String> keys = List.of(itemKey);
        List<String> args = List.of(String.valueOf(quantity));
        
        return redisTemplate.execute(updateQuantityScript, keys, args)
                .then(getCart(cartId));
    }

    public Mono<Boolean> acquireLock(String productId, String lockValue, Duration ttl) {
        String lockKey = CART_LOCK_PREFIX + productId;
        
        List<String> keys = List.of(lockKey);
        List<String> args = List.of(lockValue, String.valueOf(ttl.toMillis()));
        
        return redisTemplate.execute(acquireLockScript, keys, args)
                .next()
                .map(result -> result == 1L);
    }

    public Mono<Cart> mergeGuestCart(String userCartId, String sessionCartId) {
        String userKey = CART_PREFIX + userCartId;
        String sessionKey = SESSION_CART_PREFIX + sessionCartId;
        
        List<String> keys = List.of(userKey, sessionKey);
        
        return redisTemplate.execute(mergeCartsScript, keys, List.of())
                .then(getCart(userCartId));
    }

    public Mono<Void> deleteCart(String cartId) {
        String cartKey = CART_PREFIX + cartId;
        
        return redisTemplate.opsForSet().members(cartKey)
                .flatMap(redisTemplate.opsForValue()::delete)
                .then(redisTemplate.delete(cartKey))
                .then();
    }

    public Mono<Long> getCartItemCount(String cartId) {
        String cartKey = CART_PREFIX + cartId;
        return redisTemplate.opsForSet().size(cartKey).defaultIfEmpty(0L);
    }

    public Mono<Boolean> exists(String cartId) {
        String cartKey = CART_PREFIX + cartId;
        return redisTemplate.hasKey(cartKey);
    }

    private Mono<CartItem> getCartItemFromKey(String itemKey) {
        return redisTemplate.opsForHash().entries(itemKey)
                .collectMap(
                        entry -> entry.getKey().toString(),
                        entry -> entry.getValue().toString()
                )
                .map(map -> {
                    try {
                        String data = map.get("data");
                        CartItem item = new CartItem();
                        String[] parts = data.split("\\|");
                        item.setProductId(parts[0]);
                        item.setQuantity(Integer.parseInt(parts[1]));
                        item.setUnitPrice(Double.parseDouble(parts[2]));
                        return item;
                    } catch (Exception e) {
                        LOG.error("Failed to parse cart item", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull);
    }

    public Mono<Void> setCartTtl(String cartId, Duration ttl) {
        String cartKey = CART_PREFIX + cartId;
        return redisTemplate.expire(cartKey, ttl).then();
    }
}
