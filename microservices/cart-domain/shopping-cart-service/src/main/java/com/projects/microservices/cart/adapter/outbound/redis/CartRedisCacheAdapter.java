package com.projects.microservices.cart.adapter.outbound.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.projects.microservices.cart.application.port.outbound.CartCachePort;
import com.projects.microservices.cart.domain.model.CartEntity;

import java.time.Duration;

@Service
public class CartRedisCacheAdapter implements CartCachePort {

    private static final Logger LOG = LoggerFactory.getLogger(CartRedisCacheAdapter.class);
    private static final String CART_CACHE_PREFIX = "cart:cache:";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration cacheTtl;

    public CartRedisCacheAdapter(
            ReactiveStringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${cart.cache.ttl:3600}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.cacheTtl = Duration.ofSeconds(ttlSeconds);
    }

    @Override
    public Mono<CartEntity> get(String cartId) {
        String key = CART_CACHE_PREFIX + cartId;
        return redisTemplate.opsForValue().get(key)
                .flatMap(json -> {
                    try {
                        return Mono.just(objectMapper.readValue(json, CartEntity.class));
                    } catch (JsonProcessingException e) {
                        LOG.error("Failed to deserialize cart from cache", e);
                        return Mono.empty();
                    }
                })
                .onErrorResume(e -> {
                    LOG.error("Cache read error for cart {}", cartId, e);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> set(String cartId, CartEntity cart) {
        String key = CART_CACHE_PREFIX + cartId;
        try {
            String json = objectMapper.writeValueAsString(cart);
            return redisTemplate.opsForValue().set(key, json, cacheTtl).then();
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize cart for cache", e);
            return Mono.empty();
        }
    }

    @Override
    public Mono<Void> evict(String cartId) {
        String key = CART_CACHE_PREFIX + cartId;
        return redisTemplate.delete(key).then();
    }

    @Override
    public Mono<Boolean> exists(String cartId) {
        String key = CART_CACHE_PREFIX + cartId;
        return redisTemplate.hasKey(key);
    }
}
