package se.magnus.microservices.core.shipping.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ShippingRateRepository extends ReactiveCrudRepository<ShippingRateEntity, Long> {
    Mono<ShippingRateEntity> findByRateId(String rateId);
    Mono<ShippingRateEntity> findByOrderId(String orderId);
}
