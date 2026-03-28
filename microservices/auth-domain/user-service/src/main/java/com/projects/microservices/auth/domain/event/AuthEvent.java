package com.projects.microservices.auth.domain.event;

import java.time.Instant;

public sealed interface AuthEvent permits
    AuthEvent.UserRegistered,
    AuthEvent.UserVerified,
    AuthEvent.UserLoggedIn,
    AuthEvent.UserLocked,
    AuthEvent.PasswordResetRequested,
    AuthEvent.PasswordResetCompleted {

    record UserRegistered(
        String userId,
        String email,
        Instant timestamp
    ) implements AuthEvent {}

    record UserVerified(
        String userId,
        String email,
        Instant timestamp
    ) implements AuthEvent {}

    record UserLoggedIn(
        String userId,
        String username,
        String ipAddress,
        Instant timestamp
    ) implements AuthEvent {}

    record UserLocked(
        String userId,
        int failedAttempts,
        Instant timestamp
    ) implements AuthEvent {}

    record PasswordResetRequested(
        String userId,
        String email,
        Instant timestamp
    ) implements AuthEvent {}

    record PasswordResetCompleted(
        String userId,
        Instant timestamp
    ) implements AuthEvent {}
}
