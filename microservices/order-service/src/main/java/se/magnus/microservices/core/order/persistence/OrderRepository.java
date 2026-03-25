package se.magnus.microservices.core.order.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveCrudRepository<OrderEntity, Long> {
    Mono<OrderEntity> findByOrderId(String orderId);
    Flux<OrderEntity> findByUserId(String userId);
}
