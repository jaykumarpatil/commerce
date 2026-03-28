package se.magnus.microservices.catalog.adapter.outbound.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.microservices.catalog.domain.model.ProductEntity;

@Repository
public interface ProductMongoRepository extends ReactiveMongoRepository<ProductEntity, String> {
    Mono<ProductEntity> findByProductId(String productId);
    Flux<ProductEntity> findByCategoryId(String categoryId);
    Flux<ProductEntity> findByNameContainingIgnoreCase(String name);
}
