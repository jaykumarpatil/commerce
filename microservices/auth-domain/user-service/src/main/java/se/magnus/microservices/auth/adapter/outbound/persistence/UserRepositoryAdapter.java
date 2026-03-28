package se.magnus.microservices.auth.adapter.outbound.persistence;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import se.magnus.microservices.auth.application.port.outbound.UserRepositoryPort;
import se.magnus.microservices.auth.domain.model.UserEntity;

@Service
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserMongoRepository mongoRepository;

    public UserRepositoryAdapter(UserMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public Mono<UserEntity> save(UserEntity user) {
        return mongoRepository.save(user);
    }

    @Override
    public Mono<UserEntity> findByUserId(String userId) {
        return mongoRepository.findByUserId(userId);
    }

    @Override
    public Mono<UserEntity> findByUsername(String username) {
        return mongoRepository.findByUsername(username);
    }

    @Override
    public Mono<UserEntity> findByEmail(String email) {
        return mongoRepository.findByEmail(email);
    }

    @Override
    public Mono<Boolean> existsByUsername(String username) {
        return mongoRepository.existsByUsername(username);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return mongoRepository.existsByEmail(email);
    }

    @Override
    public Mono<Void> delete(String userId) {
        return findByUserId(userId).flatMap(mongoRepository::delete);
    }
}
