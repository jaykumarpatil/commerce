package se.magnus.microservices.auth.application.usecase;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import se.magnus.api.core.user.User;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.microservices.auth.application.port.inbound.AuthQueryPort;
import se.magnus.microservices.auth.application.port.outbound.UserRepositoryPort;
import se.magnus.microservices.auth.domain.model.UserEntity;

@Service
public class AuthQueryUseCase implements AuthQueryPort {

    private final UserRepositoryPort repository;

    public AuthQueryUseCase(UserRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Mono<User> getUser(String userId) {
        return repository.findByUserId(userId)
                .filter(user -> !user.isDeleted())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .map(this::mapToApi);
    }

    @Override
    public Mono<User> getUserByUsername(String username) {
        return repository.findByUsername(username)
                .filter(user -> !user.isDeleted())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .map(this::mapToApi);
    }

    @Override
    public Mono<User> getUserByEmail(String email) {
        return repository.findByEmail(email)
                .filter(user -> !user.isDeleted())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .map(this::mapToApi);
    }

    private User mapToApi(UserEntity entity) {
        if (entity == null) return null;
        User user = new User();
        user.setUserId(entity.getUserId());
        user.setUsername(entity.getUsername());
        user.setEmail(entity.getEmail());
        user.setFirstName(entity.getFirstName());
        user.setLastName(entity.getLastName());
        user.setRole(entity.getRole());
        user.setEnabled(entity.isEnabled());
        return user;
    }
}
