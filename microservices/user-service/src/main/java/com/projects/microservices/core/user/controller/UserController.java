package com.projects.microservices.core.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import com.projects.api.core.user.LoginRequest;
import com.projects.api.core.user.LoginResponse;
import com.projects.api.core.user.PasswordResetConfirmRequest;
import com.projects.api.core.user.PasswordResetRequest;
import com.projects.api.core.user.User;
import com.projects.api.core.user.UserService;
import com.projects.util.http.ServiceUtil;

@RestController
public class UserController implements UserService {

    private final UserService userService;
    private final ServiceUtil serviceUtil;

    @Autowired
    public UserController(UserService userService, ServiceUtil serviceUtil) {
        this.userService = userService;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<User> createUser(User user) {
        return userService.createUser(user);
    }

    @Override
    public Mono<LoginResponse> login(LoginRequest request) {
        return userService.login(request);
    }

    @Override
    public Mono<User> register(User user) {
        return userService.register(user);
    }

    @Override
    public Mono<Void> requestPasswordReset(PasswordResetRequest request) {
        return userService.requestPasswordReset(request);
    }

    @Override
    public Mono<Void> confirmPasswordReset(PasswordResetConfirmRequest request) {
        return userService.confirmPasswordReset(request);
    }

    @Override
    public Mono<User> updateUser(String userId, User user) {
        return userService.updateUser(userId, user);
    }

    @Override
    public Mono<Void> deleteUser(String userId) {
        return userService.deleteUser(userId);
    }

    @Override
    public Mono<User> enableUser(String userId) {
        return userService.enableUser(userId);
    }

    @Override
    public Mono<User> disableUser(String userId) {
        return userService.disableUser(userId);
    }

    @Override
    public Mono<User> getUser(String userId) {
        return userService.getUser(userId);
    }
}
