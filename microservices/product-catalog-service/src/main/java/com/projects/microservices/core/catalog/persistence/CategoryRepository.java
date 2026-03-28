package com.projects.microservices.core.catalog.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryRepository extends ReactiveCrudRepository<CategoryEntity, Long> {
    Mono<CategoryEntity> findByCategoryId(String categoryId);
    Mono<CategoryEntity> findBySlug(String slug);
    Flux<CategoryEntity> findAllByActiveTrueOrderBySortOrder();
}
