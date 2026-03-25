package se.magnus.microservices.core.catalog.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VariantRepository extends ReactiveCrudRepository<VariantEntity, Long> {
    Mono<VariantEntity> findByVariantId(String variantId);
    Flux<VariantEntity> findByProductId(String productId);
}
