package com.projects.microservices.core.order.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OrderEventRepository extends ReactiveCrudRepository<OrderEventEntity, Long> {
    Flux<OrderEventEntity> findByOrderIdOrderByCreatedAtAsc(String orderId);
}
