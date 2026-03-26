package se.magnus.microservices.core.user.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.magnus.microservices.core.user.persistence.UserEntity;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {

    private static final Logger LOG = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.access-token-expiry-ms:86400000}")
    private long accessTokenExpiryMs;

    @Value("${security.jwt.refresh-token-expiry-ms:604800000}")
    private long refreshTokenExpiryMs;

    @Value("${security.jwt.issuer:ecommerce-platform}")
    private String issuer;

    private final ReactiveStringRedisTemplate redisTemplate;
    private javax.crypto.SecretKey signingKey;

    public JwtTokenProvider(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            keyBytes = paddedKey;
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserEntity user) {
        Instant now = Instant.now();
        Date expiryDate = Date.from(now.plusMillis(accessTokenExpiryMs));

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getUserId())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(expiryDate)
                .claim("type", "access")
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .signWith(signingKey)
                .compact();
    }

    public Mono<String> generateAccessTokenWithRefresh(UserEntity user) {
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        
        return storeRefreshToken(user.getUserId(), refreshToken)
                .thenReturn(accessToken);
    }

    public String generateRefreshToken(UserEntity user) {
        Instant now = Instant.now();
        Date expiryDate = Date.from(now.plusMillis(refreshTokenExpiryMs));

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getUserId())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(expiryDate)
                .claim("type", "refresh")
                .claim("username", user.getUsername())
                .signWith(signingKey)
                .compact();
    }

    public Mono<String> generateRefreshTokenForUser(String userId, String username) {
        Instant now = Instant.now();
        Date expiryDate = Date.from(now.plusMillis(refreshTokenExpiryMs));

        String refreshToken = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(expiryDate)
                .claim("type", "refresh")
                .claim("username", username)
                .signWith(signingKey)
                .compact();

        return storeRefreshToken(userId, refreshToken).thenReturn(refreshToken);
    }

    private Mono<Void> storeRefreshToken(String userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        long ttlSeconds = TimeUnit.MILLISECONDS.toSeconds(refreshTokenExpiryMs);
        return redisTemplate.opsForValue().set(key, refreshToken, Duration.ofSeconds(ttlSeconds)).then();
    }

    public Mono<Void> revokeRefreshToken(String userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        return redisTemplate.delete(key).then();
    }

    public Mono<Void> revokeAllUserTokens(String userId) {
        String refreshKey = REFRESH_TOKEN_PREFIX + userId;
        String blacklistKey = BLACKLIST_PREFIX + userId;
        return redisTemplate.delete(refreshKey)
                .then(redisTemplate.opsForValue().set(blacklistKey, "1", Duration.ofHours(24)))
                .then();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return !isTokenBlacklisted(token);
        } catch (SignatureException ex) {
            LOG.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            LOG.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            LOG.error("JWT token is expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            LOG.error("JWT token is unsupported: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            LOG.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    private boolean isTokenBlacklisted(String token) {
        String tokenHash = String.valueOf(token.hashCode());
        return redisTemplate.hasKey(BLACKLIST_PREFIX + tokenHash).block();
    }

    public void blacklistToken(String token, long expiryMs) {
        String tokenHash = String.valueOf(token.hashCode());
        long ttlSeconds = TimeUnit.MILLISECONDS.toSeconds(expiryMs);
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + tokenHash, "1", Duration.ofSeconds(ttlSeconds)).subscribe();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    public String getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public String getRoleFromToken(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.get("role", String.class) : null;
    }

    public String getTokenType(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.get("type", String.class) : null;
    }

    public boolean isAccessToken(String token) {
        return "access".equals(getTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(getTokenType(token));
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            LOG.error("Failed to parse JWT claims: {}", e.getMessage());
            return null;
        }
    }

    public Mono<UserEntity> validateRefreshToken(String userId, String refreshToken) {
        if (!validateToken(refreshToken) || !isRefreshToken(refreshToken)) {
            return Mono.empty();
        }

        String storedTokenKey = REFRESH_TOKEN_PREFIX + userId;
        return redisTemplate.opsForValue().get(storedTokenKey)
                .filter(storedToken -> storedToken.equals(refreshToken))
                .map(token -> {
                    UserEntity user = new UserEntity();
                    user.setUserId(userId);
                    user.setUsername(getUsernameFromToken(token));
                    return user;
                });
    }

    public long getAccessTokenExpiryMs() {
        return accessTokenExpiryMs;
    }

    public long getRefreshTokenExpiryMs() {
        return refreshTokenExpiryMs;
    }

    public Map<String, Object> getTokenClaims(String token) {
        Claims claims = getClaims(token);
        if (claims == null) {
            return Map.of();
        }
        return Map.of(
                "sub", claims.getSubject(),
                "username", claims.get("username", String.class),
                "email", claims.get("email", String.class),
                "role", claims.get("role", String.class),
                "type", claims.get("type", String.class),
                "iss", claims.getIssuer(),
                "iat", claims.getIssuedAt(),
                "exp", claims.getExpiration()
        );
    }
}
