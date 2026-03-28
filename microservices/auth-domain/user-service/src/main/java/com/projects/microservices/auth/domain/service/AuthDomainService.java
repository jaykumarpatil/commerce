package com.projects.microservices.auth.domain.service;

import com.projects.microservices.auth.domain.model.UserEntity;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuthDomainService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    private static final int VERIFICATION_TOKEN_EXPIRY_HOURS = 24;
    private static final int PASSWORD_RESET_TOKEN_EXPIRY_MINUTES = 15;

    public void validateRegistration(UserEntity user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
    }

    public void prepareNewUser(UserEntity user) {
        user.setUserId(UUID.randomUUID().toString());
        user.setEnabled(false);
        user.setEmailVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(
            Instant.now(Clock.systemUTC()).plusSeconds(VERIFICATION_TOKEN_EXPIRY_HOURS * 3600L));
        user.setCreatedAt(Instant.now(Clock.systemUTC()));
        user.setUpdatedAt(user.getCreatedAt());
        user.setFailedLoginAttempts(0);
    }

    public boolean isAccountLocked(UserEntity user) {
        return user.getLockoutUntil() != null && 
               user.getLockoutUntil().isAfter(Instant.now());
    }

    public void handleFailedLogin(UserEntity user) {
        user.incrementFailedAttempts();
        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setLockoutUntil(
                Instant.now(Clock.systemUTC()).plusSeconds(LOCKOUT_DURATION_MINUTES * 60L));
        }
    }

    public void handleSuccessfulLogin(UserEntity user, String ip) {
        user.recordLogin(ip);
    }

    public String generatePasswordResetToken() {
        return UUID.randomUUID().toString();
    }

    public Instant getPasswordResetExpiry() {
        return Instant.now(Clock.systemUTC()).plusSeconds(PASSWORD_RESET_TOKEN_EXPIRY_MINUTES * 60L);
    }

    public boolean isPasswordResetTokenValid(UserEntity user, String token) {
        return user.getPasswordResetToken() != null &&
               user.getPasswordResetToken().equals(token) &&
               user.getPasswordResetTokenExpiry() != null &&
               user.getPasswordResetTokenExpiry().isAfter(Instant.now());
    }

    public boolean isVerificationTokenValid(UserEntity user, String token) {
        return user.getVerificationToken() != null &&
               user.getVerificationToken().equals(token) &&
               user.getVerificationTokenExpiry() != null &&
               user.getVerificationTokenExpiry().isAfter(Instant.now());
    }

    public void verifyUser(UserEntity user) {
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        user.setUpdatedAt(Instant.now(Clock.systemUTC()));
    }

    public void markUserAsDeleted(UserEntity user) {
        user.markAsDeleted();
        user.setUpdatedAt(Instant.now(Clock.systemUTC()));
    }

    public int getMinutesUntilUnlock(UserEntity user) {
        if (!isAccountLocked(user)) return 0;
        return (int) java.time.Duration.between(Instant.now(), user.getLockoutUntil()).toMinutes();
    }
}
