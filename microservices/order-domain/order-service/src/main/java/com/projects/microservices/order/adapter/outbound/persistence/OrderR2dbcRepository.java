package com.projects.microservices.order.adapter.outbound.persistence;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.projects.microservices.order.domain.model.OrderEntity;

@Repository
public interface OrderR2dbcRepository extends ReactiveCrudRepository<OrderEntity, Long> {
    Mono<OrderEntity> findByOrderId(String orderId);
    Flux<OrderEntity> findByUserId(String userId);
}
