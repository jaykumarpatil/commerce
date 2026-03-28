package se.magnus.microservices.auth.application.port.inbound;

import se.magnus.api.core.user.LoginRequest;
import se.magnus.api.core.user.LoginResponse;
import se.magnus.api.core.user.User;
import reactor.core.publisher.Mono;

public interface AuthCommandPort {
    Mono<User> register(User user);
    Mono<LoginResponse> login(LoginRequest request);
    Mono<Void> verifyEmail(String token);
    Mono<Void> requestPasswordReset(String email);
    Mono<Void> confirmPasswordReset(String token, String newPassword);
    Mono<User> updateUser(String userId, User user);
    Mono<Void> deleteUser(String userId);
}
