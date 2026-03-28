package com.projects.microservices.order.adapter.outbound.persistence;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.projects.microservices.order.application.port.outbound.OrderRepositoryPort;
import com.projects.microservices.order.domain.model.OrderEntity;

@Service
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderR2dbcRepository r2dbcRepository;

    public OrderRepositoryAdapter(OrderR2dbcRepository r2dbcRepository) {
        this.r2dbcRepository = r2dbcRepository;
    }

    @Override
    public Mono<OrderEntity> save(OrderEntity order) {
        return r2dbcRepository.save(order);
    }

    @Override
    public Mono<OrderEntity> findByOrderId(String orderId) {
        return r2dbcRepository.findByOrderId(orderId);
    }

    @Override
    public Flux<OrderEntity> findByUserId(String userId) {
        return r2dbcRepository.findByUserId(userId);
    }

    @Override
    public Mono<Void> delete(String orderId) {
        return r2dbcRepository.findByOrderId(orderId)
                .flatMap(r2dbcRepository::delete);
    }
}
