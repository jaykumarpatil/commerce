package se.magnus.microservices.auth.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import se.magnus.api.core.user.LoginRequest;
import se.magnus.api.core.user.LoginResponse;
import se.magnus.api.core.user.User;
import se.magnus.api.exceptions.BadRequestException;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.microservices.auth.application.port.inbound.AuthCommandPort;
import se.magnus.microservices.auth.application.port.outbound.AuthEventPublisherPort;
import se.magnus.microservices.auth.application.port.outbound.TokenProviderPort;
import se.magnus.microservices.auth.application.port.outbound.UserRepositoryPort;
import se.magnus.microservices.auth.domain.event.AuthEvent;
import se.magnus.microservices.auth.domain.model.UserEntity;
import se.magnus.microservices.auth.domain.service.AuthDomainService;

import java.time.Instant;

@Service
public class AuthCommandUseCase implements AuthCommandPort {

    private static final Logger LOG = LoggerFactory.getLogger(AuthCommandUseCase.class);

    private final UserRepositoryPort repository;
    private final TokenProviderPort tokenProvider;
    private final AuthEventPublisherPort eventPublisher;
    private final AuthDomainService domainService;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.jwt.access-token-expiry-ms:86400000}")
    private long accessTokenExpiry;

    public AuthCommandUseCase(UserRepositoryPort repository, TokenProviderPort tokenProvider,
                             AuthEventPublisherPort eventPublisher, AuthDomainService domainService,
                             PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.tokenProvider = tokenProvider;
        this.eventPublisher = eventPublisher;
        this.domainService = domainService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<User> register(User user) {
        return repository.existsByUsername(user.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new InvalidInputException("Username already exists"));
                    }
                    return repository.existsByEmail(user.getEmail());
                })
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new InvalidInputException("Email already exists"));
                    }
                    return createUser(user);
                });
    }

    private Mono<User> createUser(User user) {
        UserEntity entity = mapToEntity(user);
        entity.setPasswordHash(passwordEncoder.encode(entity.getPasswordHash()));
        entity.setRole("CUSTOMER");
        
        domainService.prepareNewUser(entity);
        domainService.validateRegistration(entity);

        return repository.save(entity)
                .doOnSuccess(saved -> {
                    eventPublisher.publish(new AuthEvent.UserRegistered(
                            saved.getUserId(), saved.getEmail(), Instant.now()));
                    LOG.info("User registered: {}", saved.getEmail());
                })
                .map(this::mapToApi);
    }

    @Override
    public Mono<LoginResponse> login(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return Mono.error(new InvalidInputException("Username and password are required"));
        }

        String clientIp = "unknown";

        return repository.findByUsername(request.getUsername())
                .switchIfEmpty(Mono.error(new NotFoundException("Invalid username or password")))
                .flatMap(user -> {
                    if (domainService.isAccountLocked(user)) {
                        int minutes = domainService.getMinutesUntilUnlock(user);
                        return Mono.error(new BadRequestException("Account locked. Try in " + minutes + " min"));
                    }
                    return authenticate(user, request.getPassword(), clientIp);
                });
    }

    private Mono<LoginResponse> authenticate(UserEntity user, String password, String clientIp) {
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            domainService.handleFailedLogin(user);
            return repository.save(user)
                    .flatMap(saved -> {
                        if (saved.isLocked()) {
                            eventPublisher.publish(new AuthEvent.UserLocked(
                                    saved.getUserId(), saved.getFailedLoginAttempts(), Instant.now()));
                        }
                        return Mono.<LoginResponse>error(new BadRequestException("Invalid username or password"));
                    });
        }

        if (!user.isEmailVerified()) {
            return Mono.error(new BadRequestException("Please verify your email first"));
        }

        domainService.handleSuccessfulLogin(user, clientIp);

        return repository.save(user)
                .flatMap(saved -> generateLoginResponse(saved, clientIp));
    }

    private Mono<LoginResponse> generateLoginResponse(UserEntity user, String clientIp) {
        String accessToken = tokenProvider.generateAccessToken(user.getUserId(), user.getUsername(), user.getRole());
        String refreshToken = tokenProvider.generateRefreshToken(user.getUserId());

        return Mono.just(accessToken)
                .map(token -> {
                    LoginResponse response = new LoginResponse();
                    response.setAccessToken(token);
                    response.setRefreshToken(refreshToken);
                    response.setExpiresIn(accessTokenExpiry);
                    response.setUser(mapToApi(user));
                    response.setTokenType("Bearer");
                    return response;
                })
                .doOnSuccess(r -> {
                    eventPublisher.publish(new AuthEvent.UserLoggedIn(
                            user.getUserId(), user.getUsername(), clientIp, Instant.now()));
                    LOG.info("User logged in: {}", user.getUsername());
                });
    }

    @Override
    public Mono<Void> verifyEmail(String token) {
        return repository.findByEmail(token.split("@")[0])
                .switchIfEmpty(Mono.error(new NotFoundException("Invalid verification token")))
                .flatMap(user -> {
                    if (!domainService.isVerificationTokenValid(user, token)) {
                        return Mono.error(new InvalidInputException("Invalid or expired token"));
                    }
                    domainService.verifyUser(user);
                    return repository.save(user)
                            .doOnSuccess(saved -> {
                                eventPublisher.publish(new AuthEvent.UserVerified(
                                        saved.getUserId(), saved.getEmail(), Instant.now()));
                            });
                })
                .then();
    }

    @Override
    public Mono<Void> requestPasswordReset(String email) {
        return repository.findByEmail(email)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(user -> {
                    user.setPasswordResetToken(domainService.generatePasswordResetToken());
                    user.setPasswordResetTokenExpiry(domainService.getPasswordResetExpiry());
                    return repository.save(user)
                            .doOnSuccess(saved -> {
                                eventPublisher.publish(new AuthEvent.PasswordResetRequested(
                                        saved.getUserId(), saved.getEmail(), Instant.now()));
                            });
                })
                .then();
    }

    @Override
    public Mono<Void> confirmPasswordReset(String token, String newPassword) {
        return repository.findByEmail(token.substring(0, Math.min(token.length(), 20)))
                .switchIfEmpty(Mono.error(new NotFoundException("Invalid token")))
                .flatMap(user -> {
                    if (!domainService.isPasswordResetTokenValid(user, token)) {
                        return Mono.error(new InvalidInputException("Invalid or expired token"));
                    }
                    user.setPasswordHash(passwordEncoder.encode(newPassword));
                    user.setPasswordResetToken(null);
                    user.setPasswordResetTokenExpiry(null);
                    user.setUpdatedAt(Instant.now());
                    return repository.save(user)
                            .flatMap(saved -> tokenProvider.revokeAllUserTokens(saved.getUserId()))
                            .doOnSuccess(v -> {
                                eventPublisher.publish(new AuthEvent.PasswordResetCompleted(
                                        user.getUserId(), Instant.now()));
                            });
                })
                .then();
    }

    @Override
    public Mono<User> updateUser(String userId, User user) {
        return repository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(entity -> {
                    if (user.getEmail() != null) entity.setEmail(user.getEmail());
                    if (user.getFirstName() != null) entity.setFirstName(user.getFirstName());
                    if (user.getLastName() != null) entity.setLastName(user.getLastName());
                    if (user.getRole() != null) entity.setRole(user.getRole());
                    if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
                        entity.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
                    }
                    entity.setEnabled(user.isEnabled());
                    entity.setUpdatedAt(Instant.now());
                    return repository.save(entity).map(this::mapToApi);
                });
    }

    @Override
    public Mono<Void> deleteUser(String userId) {
        return repository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(entity -> {
                    domainService.markUserAsDeleted(entity);
                    return repository.save(entity)
                            .flatMap(saved -> tokenProvider.revokeAllUserTokens(userId));
                })
                .then();
    }

    private UserEntity mapToEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setUserId(user.getUserId());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setRole(user.getRole());
        entity.setEnabled(user.isEnabled());
        return entity;
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
