package se.magnus.microservices.core.catalog.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveCrudRepository<ProductEntity, Long> {
    Mono<ProductEntity> findByProductId(String productId);
    Mono<ProductEntity> findBySlug(String slug);
    Flux<ProductEntity> findAllByActiveTrue();
    Flux<ProductEntity> findByCategoryId(String categoryId);
    Flux<ProductEntity> findByNameContainingIgnoreCaseAndActiveTrue(String name);
}
