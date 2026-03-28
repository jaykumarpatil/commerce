package com.projects.microservices.core.user.services.email;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.projects.microservices.core.user.persistence.UserEntity;
import com.projects.microservices.core.user.persistence.UserRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class EmailVerificationService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final String VERIFICATION_PREFIX = "email:verify:";
    private static final String PASSWORD_RESET_PREFIX = "password:reset:";
    private static final Duration VERIFICATION_TTL = Duration.ofHours(24);
    private static final Duration PASSWORD_RESET_TTL = Duration.ofHours(1);

    private final UserRepository userRepository;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@ecommerce.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${email.verification.enabled:true}")
    private boolean emailVerificationEnabled;

    private SecureRandom secureRandom;

    public EmailVerificationService(UserRepository userRepository,
                                    ReactiveStringRedisTemplate redisTemplate,
                                    JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void init() {
        try {
            this.secureRandom = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            this.secureRandom = new SecureRandom();
        }
    }

    public Mono<UserEntity> sendVerificationEmail(UserEntity user) {
        if (!emailVerificationEnabled) {
            user.setEmailVerified(true);
            return userRepository.save(user).thenReturn(user);
        }

        String token = generateSecureToken();
        String hashedToken = hashToken(token);
        
        user.setVerificationToken(hashedToken);
        user.setVerificationTokenExpiry(Instant.now().plus(VERIFICATION_TTL));
        user.setEmailVerified(false);

        return userRepository.save(user)
                .flatMap(savedUser -> {
                    storeVerificationToken(savedUser.getUserId(), hashedToken);
                    return sendVerificationEmailAsync(savedUser.getEmail(), token, savedUser.getUsername());
                })
                .thenReturn(user);
    }

    private Mono<Void> sendVerificationEmailAsync(String email, String token, String username) {
        return Mono.fromRunnable(() -> {
            try {
                String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + token;
                
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(email);
                message.setSubject("Verify your email address");
                message.setText(buildVerificationEmailBody(username, verificationUrl));
                
                mailSender.send(message);
                LOG.info("Verification email sent to: {}", email);
            } catch (Exception e) {
                LOG.error("Failed to send verification email to: {}", email, e);
            }
        });
    }

    public Mono<UserEntity> verifyEmailToken(String token) {
        String hashedToken = hashToken(token);
        String key = VERIFICATION_PREFIX + hashedToken;
        
        return redisTemplate.opsForValue().get(key)
                .flatMap(userId -> userRepository.findByUserId(userId)
                        .filter(user -> !user.isEmailVerified())
                        .filter(user -> user.getVerificationToken() != null)
                        .filter(user -> user.getVerificationToken().equals(hashedToken))
                        .filter(user -> user.getVerificationTokenExpiry() != null)
                        .filter(user -> user.getVerificationTokenExpiry().isAfter(Instant.now()))
                        .flatMap(user -> {
                            user.setEmailVerified(true);
                            user.setVerificationToken(null);
                            user.setVerificationTokenExpiry(null);
                            return userRepository.save(user);
                        })
                )
                .doOnSuccess(user -> {
                    if (user != null) {
                        redisTemplate.delete(key).subscribe();
                        LOG.info("Email verified for user: {}", user.getUserId());
                    }
                });
    }

    public Mono<String> initiatePasswordReset(String email) {
        return userRepository.findByEmail(email)
                .filter(user -> !user.isDeleted())
                .flatMap(user -> {
                    String token = generateSecureToken();
                    String hashedToken = hashToken(token);
                    
                    user.setPasswordResetToken(hashedToken);
                    user.setPasswordResetTokenExpiry(Instant.now().plus(PASSWORD_RESET_TTL));
                    
                    return userRepository.save(user)
                            .then(storePasswordResetToken(user.getUserId(), hashedToken))
                            .then(sendPasswordResetEmailAsync(email, token, user.getUsername()))
                            .thenReturn(token);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    LOG.info("Password reset requested for non-existent email: {}", email);
                    return Mono.empty();
                }));
    }

    private Mono<Void> sendPasswordResetEmailAsync(String email, String token, String username) {
        return Mono.fromRunnable(() -> {
            try {
                String resetUrl = baseUrl + "/api/auth/reset-password?token=" + token;
                
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(email);
                message.setSubject("Password Reset Request");
                message.setText(buildPasswordResetEmailBody(username, resetUrl));
                
                mailSender.send(message);
                LOG.info("Password reset email sent to: {}", email);
            } catch (Exception e) {
                LOG.error("Failed to send password reset email to: {}", email, e);
            }
        });
    }

    public Mono<UserEntity> resetPassword(String token, String newPassword) {
        String hashedToken = hashToken(token);
        
        return userRepository.findByEmailVerificationTokenOrPasswordResetToken(hashedToken, hashedToken)
                .filter(user -> user.getPasswordResetToken() != null)
                .filter(user -> user.getPasswordResetToken().equals(hashedToken))
                .filter(user -> user.getPasswordResetTokenExpiry() != null)
                .filter(user -> user.getPasswordResetTokenExpiry().isAfter(Instant.now()))
                .flatMap(user -> {
                    user.setPasswordResetToken(null);
                    user.setPasswordResetTokenExpiry(null);
                    user.setUpdatedAt(Instant.now());
                    return userRepository.save(user);
                });
    }

    private void storeVerificationToken(String userId, String hashedToken) {
        String key = VERIFICATION_PREFIX + hashedToken;
        redisTemplate.opsForValue().set(key, userId, VERIFICATION_TTL).subscribe();
    }

    private Mono<Void> storePasswordResetToken(String userId, String hashedToken) {
        String key = PASSWORD_RESET_PREFIX + hashedToken;
        return redisTemplate.opsForValue().set(key, userId, PASSWORD_RESET_TTL).then();
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String buildVerificationEmailBody(String username, String verificationUrl) {
        return String.format("""
            Hello %s,
            
            Thank you for registering with us! Please verify your email address by clicking the link below:
            
            %s
            
            This link will expire in 24 hours.
            
            If you didn't create an account, please ignore this email.
            
            Best regards,
            The E-Commerce Team
            """, username, verificationUrl);
    }

    private String buildPasswordResetEmailBody(String username, String resetUrl) {
        return String.format("""
            Hello %s,
            
            We received a request to reset your password. Click the link below to create a new password:
            
            %s
            
            This link will expire in 1 hour.
            
            If you didn't request a password reset, please ignore this email or contact support if you have concerns.
            
            Best regards,
            The E-Commerce Team
            """, username, resetUrl);
    }
}
