package se.magnus.microservices.auth.adapter.outbound.external;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import se.magnus.microservices.auth.application.port.outbound.TokenProviderPort;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtTokenProviderAdapter implements TokenProviderPort {

    private final SecretKey secretKey;
    private final long accessTokenExpiryMs;
    private final ReactiveStringRedisTemplate redisTemplate;

    public JwtTokenProviderAdapter(
            @Value("${security.jwt.secret:your-256-bit-secret-key-for-jwt-token-signing}") String secret,
            @Value("${security.jwt.access-token-expiry-ms:86400000}") long accessTokenExpiryMs,
            ReactiveStringRedisTemplate redisTemplate) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String generateAccessToken(String userId, String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiryMs);

        return Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String generateRefreshToken(String userId) {
        return "refresh_" + userId + "_" + System.currentTimeMillis();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Mono<Void> revokeToken(String userId) {
        return redisTemplate.opsForValue().delete("token:" + userId).then();
    }

    @Override
    public Mono<Void> revokeAllUserTokens(String userId) {
        return redisTemplate.delete(redisTemplate.keys("token:" + userId + "*")).then();
    }
}
