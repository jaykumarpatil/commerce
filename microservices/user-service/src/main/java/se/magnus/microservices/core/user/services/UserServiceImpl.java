package se.magnus.microservices.core.user.services;

import static java.util.logging.Level.FINE;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
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
import se.magnus.util.http.ServiceUtil;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository repository;
    private final UserMapper mapper;
    private final ServiceUtil serviceUtil;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${security.jwt.secret-key:my-secret-key}")
    private String secretKey;

    @Autowired
    public UserServiceImpl(UserRepository repository, UserMapper mapper, ServiceUtil serviceUtil,
            PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Mono<User> createUser(User body) {
        if (body.getUserId() == null || body.getUserId().isEmpty()) {
            throw new InvalidInputException("Invalid userId: " + body.getUserId());
        }

        UserEntity entity = mapper.apiToEntity(body);
        entity.setPasswordHash(passwordEncoder.encode(entity.getPasswordHash()));

        Mono<User> newEntity = repository.save(entity)
                .log(LOG.getName(), FINE)
                .onErrorMap(DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, User Id: " + body.getUserId()))
                .map(e -> mapper.entityToApi(e));

        return newEntity;
    }

    @Override
    public Mono<User> register(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new InvalidInputException("Invalid email: " + user.getEmail());
        }
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new InvalidInputException("Invalid username: " + user.getUsername());
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            throw new InvalidInputException("Invalid password: " + user.getPasswordHash());
        }

        // Check if username or email already exists
        return repository.findByUsername(user.getUsername())
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new InvalidInputException("Username already exists: " + user.getUsername()));
                    }
                    return repository.findByEmail(user.getEmail())
                            .hasElement()
                            .flatMap(emailExists -> {
                                if (emailExists) {
                                    return Mono.error(new InvalidInputException("Email already exists: " + user.getEmail()));
                                }
                                // Create new user
                                UserEntity entity = mapper.apiToEntity(user);
                                entity.setUserId(UUID.randomUUID().toString());
                                entity.setPasswordHash(passwordEncoder.encode(entity.getPasswordHash()));
                                entity.setRole("CUSTOMER");
                                entity.setEnabled(true);
                                entity.setCreatedAt(ZonedDateTime.now(Clock.systemUTC()).toString());
                                entity.setUpdatedAt(entity.getCreatedAt());

                                return repository.save(entity)
                                        .log(LOG.getName(), FINE)
                                        .map(e -> mapper.entityToApi(e));
                            });
                });
    }

    @Override
    public Mono<LoginResponse> login(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return Mono.error(new InvalidInputException("Username and password are required"));
        }

        LOG.info("Login attempt for username: {}", request.getUsername());

        return repository.findByUsername(request.getUsername())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + request.getUsername())))
                .flatMap(userEntity -> {
                    if (!passwordEncoder.matches(request.getPassword(), userEntity.getPasswordHash())) {
                        return Mono.error(new BadRequestException("Invalid password"));
                    }
                    if (!userEntity.isEnabled()) {
                        return Mono.error(new BadRequestException("User is disabled"));
                    }

                    // Generate tokens
                    String accessToken = jwtTokenProvider.generateAccessToken(userEntity);
                    String refreshToken = jwtTokenProvider.generateRefreshToken(userEntity);

                    LoginResponse response = new LoginResponse();
                    response.setAccessToken(accessToken);
                    response.setRefreshToken(refreshToken);
                    response.setExpiresIn(jwtTokenProvider.getExpirationTime());
                    response.setUser(mapper.entityToApi(userEntity));

                    return Mono.just(response);
                })
                .log(LOG.getName(), FINE);
    }

    @Override
    public Mono<User> updateUser(String userId, User user) {
        return repository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + userId)))
                .flatMap(entity -> {
                    if (user.getEmail() != null) entity.setEmail(user.getEmail());
                    if (user.getFirstName() != null) entity.setFirstName(user.getFirstName());
                    if (user.getLastName() != null) entity.setLastName(user.getLastName());
                    if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
                        entity.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
                    }
                    if (user.getRole() != null) entity.setRole(user.getRole());
                    entity.setEnabled(user.isEnabled());
                    entity.setUpdatedAt(ZonedDateTime.now(Clock.systemUTC()).toString());

                    return repository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(e -> mapper.entityToApi(e));
                });
    }

    @Override
    public Mono<Void> deleteUser(String userId) {
        return repository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + userId)))
                .flatMap(repository::delete)
                .then(Mono.empty());
    }

    @Override
    public Mono<User> enableUser(String userId) {
        return updateUser(userId, createUserBuilder(userId).enabled(true).build());
    }

    @Override
    public Mono<User> disableUser(String userId) {
        return updateUser(userId, createUserBuilder(userId).enabled(false).build());
    }

    @Override
    public Mono<Void> requestPasswordReset(PasswordResetRequest request) {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return Mono.error(new InvalidInputException("Email is required"));
        }

        return repository.findByEmail(request.getEmail())
                .switchIfEmpty(Mono.empty())
                .flatMap(entity -> {
                    String token = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
                    // TODO: Send password reset email with token
                    LOG.info("Password reset requested for email: {}, token: {}", request.getEmail(), token);
                    return Mono.just(entity);
                })
                .then(Mono.empty());
    }

    @Override
    public Mono<Void> confirmPasswordReset(PasswordResetConfirmRequest request) {
        if (request.getToken() == null || request.getToken().isEmpty()) {
            return Mono.error(new InvalidInputException("Token is required"));
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return Mono.error(new InvalidInputException("Password is required"));
        }

        // TODO: Validate token and update password
        LOG.info("Confirming password reset for token: {}", request.getToken());
        
        // For now, just log the request
        return Mono.empty();
    }

    private User.Builder createUserBuilder(String userId) {
        return User.builder().userId(userId);
    }
}
