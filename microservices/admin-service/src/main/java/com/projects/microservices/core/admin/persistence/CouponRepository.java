package com.projects.microservices.core.admin.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CouponRepository extends ReactiveCrudRepository<CouponEntity, Long> {
    Mono<CouponEntity> findByCouponId(String couponId);
}
