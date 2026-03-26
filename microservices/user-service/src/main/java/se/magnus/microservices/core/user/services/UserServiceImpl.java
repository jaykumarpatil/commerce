package se.magnus.microservices.core.user.services;

import static java.util.logging.Level.FINE;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import se.magnus.api.core.user.LoginRequest;
import se.magnus.api.core.user.LoginResponse;
import se.magnus.api.core.user.PasswordResetConfirmRequest;
import se.magnus.api.core.user.PasswordResetRequest;
import se.magnus.api.core.user.User;
import se.magnus.api.core.user.UserService;
import se.magnus.api.exceptions.BadRequestException;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.microservices.core.user.persistence.UserEntity;
import se.magnus.microservices.core.user.persistence.UserRepository;
import se.magnus.microservices.core.user.services.email.EmailVerificationService;
import se.magnus.util.http.ServiceUtil;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    private final UserRepository repository;
    private final UserMapper mapper;
    private final ServiceUtil serviceUtil;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordValidationService passwordValidation;
    private final EmailValidationService emailValidation;
    private final EmailVerificationService emailVerification;

    @Value("${security.jwt.access-token-expiry-ms:86400000}")
    private long accessTokenExpiry;

    @Autowired
    public UserServiceImpl(UserRepository repository, UserMapper mapper, ServiceUtil serviceUtil,
                           PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider,
                           PasswordValidationService passwordValidation, EmailValidationService emailValidation,
                           EmailVerificationService emailVerification) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordValidation = passwordValidation;
        this.emailValidation = emailValidation;
        this.emailVerification = emailVerification;
    }

    @PostConstruct
    public void init() {
        LOG.info("UserService initialized with access token expiry: {}ms", accessTokenExpiry);
    }

    @Override
    public Mono<User> createUser(User body) {
        if (body.getUserId() == null || body.getUserId().isEmpty()) {
            throw new InvalidInputException("Invalid userId: " + body.getUserId());
        }
        UserEntity entity = mapper.apiToEntity(body);
        entity.setPasswordHash(passwordEncoder.encode(entity.getPasswordHash()));
        return repository.save(entity)
                .log(LOG.getName(), FINE)
                .onErrorMap(DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, User Id: " + body.getUserId()))
                .map(mapper::entityToApi);
    }

    @Override
    public Mono<User> register(User user) {
        if (!emailValidation.isValidEmail(user.getEmail())) {
            List<String> errors = emailValidation.getValidationErrors(user.getEmail());
            return Mono.error(new InvalidInputException("Invalid email: " + String.join(", ", errors)));
        }
        
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            return Mono.error(new InvalidInputException("Username is required"));
        }
        
        if (!passwordValidation.isPasswordStrong(user.getPasswordHash())) {
            String feedback = passwordValidation.getPasswordStrengthFeedback(user.getPasswordHash());
            return Mono.error(new InvalidInputException(feedback));
        }
        
        if (passwordValidation.isCommonPassword(user.getPasswordHash())) {
            return Mono.error(new InvalidInputException("Password is too common. Please choose a stronger password."));
        }

        user.setEmail(emailValidation.normalizeEmail(user.getEmail()));

        return repository.findByUsername(user.getUsername())
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new InvalidInputException("Username already exists"));
                    }
                    return repository.findByEmail(user.getEmail());
                })
                .flatMap(existingUser -> {
                    if (existingUser != null) {
                        return Mono.error(new InvalidInputException("Email already exists"));
                    }
                    return createUserEntity(user);
                });
    }

    private Mono<User> createUserEntity(User user) {
        UserEntity entity = mapper.apiToEntity(user);
        entity.setUserId(UUID.randomUUID().toString());
        entity.setPasswordHash(passwordEncoder.encode(entity.getPasswordHash()));
        entity.setRole("CUSTOMER");
        entity.setEnabled(false);
        entity.setEmailVerified(false);
        entity.setCreatedAt(Instant.now(Clock.systemUTC()));
        entity.setUpdatedAt(entity.getCreatedAt());
        entity.setFailedLoginAttempts(0);

        return repository.save(entity)
                .flatMap(savedUser -> emailVerification.sendVerificationEmail(savedUser))
                .map(mapper::entityToApi)
                .doOnSuccess(u -> LOG.info("User registered: {}", u.getEmail()));
    }

    @Override
    public Mono<LoginResponse> login(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return Mono.error(new InvalidInputException("Username and password are required"));
        }

        String clientIp = "unknown";
        
        return repository.findByUsername(request.getUsername())
                .switchIfEmpty(Mono.error(new NotFoundException("Invalid username or password")))
                .flatMap(user -> checkLockout(user, clientIp)
                        .flatMap(lockedUser -> authenticateUser(lockedUser, request.getPassword(), clientIp))
                );
    }

    private Mono<UserEntity> checkLockout(UserEntity user, String clientIp) {
        if (user.getLockoutUntil() != null && user.getLockoutUntil().isAfter(Instant.now())) {
            long minutesLeft = java.time.Duration.between(Instant.now(), user.getLockoutUntil()).toMinutes();
            return Mono.error(new BadRequestException("Account is locked. Try again in " + minutesLeft + " minutes."));
        }
        return Mono.just(user);
    }

    private Mono<LoginResponse> authenticateUser(UserEntity user, String password, String clientIp) {
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return handleFailedLogin(user, clientIp);
        }

        if (!user.isEmailVerified()) {
            return Mono.error(new BadRequestException("Please verify your email before logging in"));
        }

        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);
        user.setLastLoginAt(Instant.now(Clock.systemUTC()));
        user.setLastLoginIp(clientIp);
        user.setUpdatedAt(Instant.now(Clock.systemUTC()));

        return repository.save(user)
                .flatMap(updatedUser -> generateTokens(updatedUser));
    }

    private Mono<LoginResponse> handleFailedLogin(UserEntity user, String clientIp) {
        int failedAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(failedAttempts);
        user.setUpdatedAt(Instant.now(Clock.systemUTC()));

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockoutUntil(Instant.now(Clock.systemUTC()).plusSeconds(LOCKOUT_DURATION_MINUTES * 60L));
            LOG.warn("User {} locked out due to {} failed attempts from IP: {}",
                    user.getUsername(), failedAttempts, clientIp);
        }

        return repository.save(user)
                .then(Mono.error(new BadRequestException("Invalid username or password")));
    }

    private Mono<LoginResponse> generateTokens(UserEntity user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        
        return jwtTokenProvider.generateAccessTokenWithRefresh(user)
                .map(token -> {
                    LoginResponse response = new LoginResponse();
                    response.setAccessToken(token);
                    response.setRefreshToken(jwtTokenProvider.generateRefreshToken(user));
                    response.setExpiresIn(accessTokenExpiry);
                    response.setUser(mapper.entityToApi(user));
                    response.setTokenType("Bearer");
                    return response;
                })
                .doOnSuccess(r -> LOG.info("User {} logged in successfully", user.getUsername()));
    }

    @Override
    public Mono<User> updateUser(String userId, User user) {
        return repository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + userId)))
                .flatMap(entity -> {
                    if (user.getEmail() != null) {
                        if (!emailValidation.isValidEmail(user.getEmail())) {
                            return Mono.error(new InvalidInputException("Invalid email format"));
                        }
                        entity.setEmail(emailValidation.normalizeEmail(user.getEmail()));
                    }
                    if (user.getFirstName() != null) entity.setFirstName(user.getFirstName());
                    if (user.getLastName() != null) entity.setLastName(user.getLastName());
                    if (user.getRole() != null) entity.setRole(user.getRole());
                    if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
                        if (!passwordValidation.isPasswordStrong(user.getPasswordHash())) {
                            return Mono.error(new InvalidInputException("Password does not meet requirements"));
                        }
                        entity.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
                    }
                    entity.setEnabled(user.isEnabled());
                    entity.setUpdatedAt(Instant.now(Clock.systemUTC()));
                    return repository.save(entity).map(mapper::entityToApi);
                });
    }

    @Override
    public Mono<Void> deleteUser(String userId) {
        return repository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + userId)))
                .flatMap(user -> {
                    user.setDeleted(true);
                    user.setDeletedAt(Instant.now(Clock.systemUTC()));
                    user.setEmail("deleted_" + user.getEmail());
                    user.setUsername("deleted_" + user.getUserId());
                    user.setEnabled(false);
                    return repository.save(user);
                })
                .then(jwtTokenProvider.revokeAllUserTokens(userId))
                .then();
    }

    @Override
    public Mono<User> enableUser(String userId) {
        return repository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + userId)))
                .flatMap(entity -> {
                    entity.setEnabled(true);
                    entity.setUpdatedAt(Instant.now(Clock.systemUTC()));
                    return repository.save(entity).map(mapper::entityToApi);
                });
    }

    @Override
    public Mono<User> disableUser(String userId) {
        return repository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + userId)))
                .flatMap(entity -> {
                    entity.setEnabled(false);
                    entity.setUpdatedAt(Instant.now(Clock.systemUTC()));
                    return repository.save(entity).map(mapper::entityToApi);
                })
                .flatMap(user -> jwtTokenProvider.revokeAllUserTokens(userId).thenReturn(user));
    }

    @Override
    public Mono<Void> requestPasswordReset(PasswordResetRequest request) {
        if (!emailValidation.isValidEmail(request.getEmail())) {
            return Mono.error(new InvalidInputException("Invalid email format"));
        }
        return emailVerification.initiatePasswordReset(emailValidation.normalizeEmail(request.getEmail()))
                .then();
    }

    @Override
    public Mono<Void> confirmPasswordReset(PasswordResetConfirmRequest request) {
        if (request.getToken() == null || request.getToken().isBlank()) {
            return Mono.error(new InvalidInputException("Token is required"));
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return Mono.error(new InvalidInputException("Password is required"));
        }
        if (!passwordValidation.isPasswordStrong(request.getPassword())) {
            return Mono.error(new InvalidInputException("Password does not meet requirements"));
        }
        return emailVerification.resetPassword(request.getToken(), request.getPassword())
                .switchIfEmpty(Mono.error(new InvalidInputException("Invalid or expired token")))
                .flatMap(user -> {
                    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                    user.setUpdatedAt(Instant.now(Clock.systemUTC()));
                    return repository.save(user);
                })
                .flatMap(user -> jwtTokenProvider.revokeAllUserTokens(user.getUserId()).thenReturn(user))
                .then();
    }

    public Mono<UserEntity> getUserById(String userId) {
        return repository.findByUserId(userId)
                .filter(user -> !user.isDeleted())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + userId)));
    }

    public Mono<UserEntity> getUserByUsername(String username) {
        return repository.findByUsername(username)
                .filter(user -> !user.isDeleted())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + username)));
    }

    public Mono<UserEntity> getUserByEmail(String email) {
        return repository.findByEmail(email)
                .filter(user -> !user.isDeleted())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found with email: " + email)));
    }

    public Mono<List<UserEntity>> searchUsers(String query, String role, int page, int size) {
        return repository.findByEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(query, query)
                .filter(user -> !user.isDeleted())
                .filter(user -> role == null || role.isEmpty() || user.getRole().equals(role))
                .skip((long) page * size)
                .take(size)
                .collectList();
    }
}
