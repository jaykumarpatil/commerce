package se.magnus.microservices.core.admin.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdminUserRepository extends ReactiveCrudRepository<AdminUserEntity, Long> {
    Mono<AdminUserEntity> findByUserId(String userId);
}
