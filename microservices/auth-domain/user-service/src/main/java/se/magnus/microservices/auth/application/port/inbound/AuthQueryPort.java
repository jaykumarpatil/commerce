package se.magnus.microservices.auth.application.port.inbound;

import se.magnus.api.core.user.User;
import reactor.core.publisher.Mono;

public interface AuthQueryPort {
    Mono<User> getUser(String userId);
    Mono<User> getUserByUsername(String username);
    Mono<User> getUserByEmail(String email);
}
