package se.magnus.microservices.core.shipping.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ShippingAddressRepository extends ReactiveCrudRepository<ShippingAddressEntity, Long> {
    Mono<ShippingAddressEntity> findByAddressId(String addressId);
    Flux<ShippingAddressEntity> findByUserId(String userId);
}
