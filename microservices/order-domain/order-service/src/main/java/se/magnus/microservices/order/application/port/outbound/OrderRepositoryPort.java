package se.magnus.microservices.order.application.port.outbound;

import se.magnus.microservices.order.domain.model.OrderEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepositoryPort {
    Mono<OrderEntity> save(OrderEntity order);
    Mono<OrderEntity> findByOrderId(String orderId);
    Flux<OrderEntity> findByUserId(String userId);
    Mono<Void> delete(String orderId);
}
