package se.magnus.microservices.core.inventory.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InventoryRepository extends ReactiveCrudRepository<InventoryEntity, Long> {
    Mono<InventoryEntity> findByInventoryId(String inventoryId);
    Mono<InventoryEntity> findByProductId(String productId);
}
