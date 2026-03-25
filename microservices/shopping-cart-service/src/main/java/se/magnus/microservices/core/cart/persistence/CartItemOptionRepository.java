package se.magnus.microservices.core.cart.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CartItemOptionRepository extends ReactiveMongoRepository<CartItemOptionEntity, String> {
    Mono<CartItemOptionEntity> findByOptionName(String optionName);
}
