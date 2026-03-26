package se.magnus.microservices.core.user.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<UserEntity, String> {
    Mono<UserEntity> findByUsername(String username);
    Mono<UserEntity> findByEmail(String email);
    Mono<UserEntity> findByUserId(String userId);
    Mono<Boolean> existsByUsername(String username);
    Mono<Boolean> existsByEmail(String email);
    
    @Query("{ '$or': [ { 'email': { '$regex': ?0, '$options': 'i' } }, { 'username': { '$regex': ?0, '$options': 'i' } } ] }")
    Flux<UserEntity> findByEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(String email, String username);
    
    Mono<UserEntity> findByEmailVerificationTokenOrPasswordResetToken(String verificationToken, String resetToken);
    
    Flux<UserEntity> findByRole(String role);
    
    Flux<UserEntity> findByRoleAndEnabled(String role, boolean enabled);
    
    @Query("{ 'createdAt': { '$gte': ?0, '$lte': ?1 } }")
    Flux<UserEntity> findByCreatedAtBetween(java.time.Instant start, java.time.Instant end);
    
    Flux<UserEntity> findByDeletedFalse();
    
    Mono<UserEntity> findByEmailAndDeletedFalse(String email);
}
