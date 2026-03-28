package com.projects.api.core.user;

import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;

public interface UserService {

    @PostMapping("/v1/users")
    Mono<User> createUser(User user);

    @PostMapping("/v1/users/login")
    Mono<LoginResponse> login(LoginRequest request);

    @PostMapping("/v1/users/register")
    Mono<User> register(User user);

    @PostMapping("/v1/users/password-reset")
    Mono<Void> requestPasswordReset(PasswordResetRequest request);

    @PostMapping("/v1/users/password-reset/confirm")
    Mono<Void> confirmPasswordReset(PasswordResetConfirmRequest request);

    @PostMapping("/v1/users/{userId}")
    Mono<User> updateUser(String userId, User user);

    @PostMapping("/v1/users/{userId}")
    Mono<Void> deleteUser(String userId);

    @PostMapping("/v1/users/{userId}/enable")
    Mono<User> enableUser(String userId);

    @PostMapping("/v1/users/{userId}/disable")
    Mono<User> disableUser(String userId);
}
