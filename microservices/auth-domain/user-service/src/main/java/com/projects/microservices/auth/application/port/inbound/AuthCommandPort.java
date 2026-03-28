package com.projects.microservices.auth.application.port.inbound;

import com.projects.api.core.user.LoginRequest;
import com.projects.api.core.user.LoginResponse;
import com.projects.api.core.user.User;
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
