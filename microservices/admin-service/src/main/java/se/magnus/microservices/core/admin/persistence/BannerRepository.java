package se.magnus.microservices.core.admin.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BannerRepository extends ReactiveCrudRepository<BannerEntity, Long> {
    Mono<BannerEntity> findByBannerId(String bannerId);
}
