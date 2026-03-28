package se.magnus.microservices.auth.adapter.inbound.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import se.magnus.api.core.user.LoginRequest;
import se.magnus.api.core.user.LoginResponse;
import se.magnus.api.core.user.User;
import se.magnus.microservices.auth.application.port.inbound.AuthCommandPort;
import se.magnus.microservices.auth.application.port.inbound.AuthQueryPort;

@RestController
public class AuthController {

    private final AuthCommandPort commandPort;
    private final AuthQueryPort queryPort;

    @Autowired
    public AuthController(AuthCommandPort commandPort, AuthQueryPort queryPort) {
        this.commandPort = commandPort;
        this.queryPort = queryPort;
    }

    public Mono<User> register(User user) {
        return commandPort.register(user);
    }

    public Mono<LoginResponse> login(LoginRequest request) {
        return commandPort.login(request);
    }

    public Mono<Void> verifyEmail(String token) {
        return commandPort.verifyEmail(token);
    }

    public Mono<Void> requestPasswordReset(String email) {
        return commandPort.requestPasswordReset(email);
    }

    public Mono<Void> confirmPasswordReset(String token, String newPassword) {
        return commandPort.confirmPasswordReset(token, newPassword);
    }

    public Mono<User> updateUser(String userId, User user) {
        return commandPort.updateUser(userId, user);
    }

    public Mono<Void> deleteUser(String userId) {
        return commandPort.deleteUser(userId);
    }

    public Mono<User> getUser(String userId) {
        return queryPort.getUser(userId);
    }

    public Mono<User> getUserByUsername(String username) {
        return queryPort.getUserByUsername(username);
    }
}
