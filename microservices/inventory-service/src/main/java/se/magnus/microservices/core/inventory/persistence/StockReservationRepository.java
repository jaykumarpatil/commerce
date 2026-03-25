package se.magnus.microservices.core.inventory.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StockReservationRepository extends ReactiveCrudRepository<StockReservationEntity, Long> {
    Mono<StockReservationEntity> findByReservationId(String reservationId);
    Flux<StockReservationEntity> findByOrderId(String orderId);
}
