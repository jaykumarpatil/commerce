package se.magnus.microservices.auth.adapter.outbound.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import se.magnus.microservices.auth.domain.model.UserEntity;

@Repository
public interface UserMongoRepository extends ReactiveMongoRepository<UserEntity, String> {
    Mono<UserEntity> findByUserId(String userId);
    Mono<UserEntity> findByUsername(String username);
    Mono<UserEntity> findByEmail(String email);
    Mono<Boolean> existsByUsername(String username);
    Mono<Boolean> existsByEmail(String email);
}
