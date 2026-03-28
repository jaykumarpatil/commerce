package com.projects.microservices.inventory.adapter.outbound.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import com.projects.microservices.inventory.domain.model.InventoryEntity;

@Repository
public interface InventoryMongoRepository extends ReactiveMongoRepository<InventoryEntity, String> {
    Mono<InventoryEntity> findByProductId(String productId);
}
