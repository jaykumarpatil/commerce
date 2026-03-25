package se.magnus.microservices.core.order.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItemEntity, Long> {
    Mono<OrderItemEntity> findByOrderItemId(String orderItemId);
    Flux<OrderItemEntity> findByOrderId(String orderId);
}
