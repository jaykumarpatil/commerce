package se.magnus.microservices.core.cart.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CartRepository extends ReactiveMongoRepository<CartEntity, String> {
    Mono<CartEntity> findByCartId(String cartId);
    Mono<CartEntity> findByUserId(String userId);
}
