package com.projects.microservices.cart.adapter.outbound.persistence;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.projects.microservices.cart.application.port.outbound.CartRepositoryPort;
import com.projects.microservices.cart.domain.model.CartEntity;

@Service
public class CartRepositoryAdapter implements CartRepositoryPort {

    private final CartMongoRepository mongoRepository;

    public CartRepositoryAdapter(CartMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public Mono<CartEntity> save(CartEntity cart) {
        return mongoRepository.save(cart);
    }

    @Override
    public Mono<CartEntity> findByCartId(String cartId) {
        return mongoRepository.findByCartId(cartId);
    }

    @Override
    public Mono<CartEntity> findByUserId(String userId) {
        return mongoRepository.findByUserId(userId);
    }

    @Override
    public Mono<Void> delete(String cartId) {
        return mongoRepository.findByCartId(cartId)
                .flatMap(mongoRepository::delete);
    }

    @Override
    public Mono<Void> deleteByUserId(String userId) {
        return mongoRepository.findByUserId(userId)
                .flatMap(mongoRepository::delete);
    }
}
