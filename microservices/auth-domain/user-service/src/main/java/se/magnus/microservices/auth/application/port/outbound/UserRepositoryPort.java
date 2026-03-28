package se.magnus.microservices.auth.application.port.outbound;

import se.magnus.microservices.auth.domain.model.UserEntity;
import reactor.core.publisher.Mono;

public interface UserRepositoryPort {
    Mono<UserEntity> save(UserEntity user);
    Mono<UserEntity> findByUserId(String userId);
    Mono<UserEntity> findByUsername(String username);
    Mono<UserEntity> findByEmail(String email);
    Mono<Boolean> existsByUsername(String username);
    Mono<Boolean> existsByEmail(String email);
    Mono<Void> delete(String userId);
}
