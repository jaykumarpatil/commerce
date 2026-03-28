package com.projects.microservices.core.shipping.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ShipmentRepository extends ReactiveCrudRepository<ShipmentEntity, Long> {
    Mono<ShipmentEntity> findByShipmentId(String shipmentId);
    Mono<ShipmentEntity> findByOrderId(String orderId);
    Mono<ShipmentEntity> findByTrackingNumber(String trackingNumber);
}
