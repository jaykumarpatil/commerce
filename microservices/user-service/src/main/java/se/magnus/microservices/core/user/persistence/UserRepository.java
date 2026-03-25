package se.magnus.microservices.core.user.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<UserEntity, String> {
    Mono<UserEntity> findByUsername(String username);
    Mono<UserEntity> findByEmail(String email);
    Mono<UserEntity> findByUserId(String userId);
}
