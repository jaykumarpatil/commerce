package se.magnus.microservices.core.user.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter implements WebFilter {

    private static final Logger LOG = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final String RATE_LIMIT_PREFIX = "ratelimit:";
    private static final String[] LIMITED_PATHS = {"/login", "/register", "/api/auth/login"};

    private final ReactiveStringRedisTemplate redisTemplate;
    private final Map<String, Long> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> windowStarts = new ConcurrentHashMap<>();

    @Value("${security.rate-limit.requests-per-minute:5}")
    private int maxRequestsPerMinute;

    @Value("${security.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    public RateLimitFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        
        if (!shouldRateLimit(path)) {
            return chain.filter(exchange);
        }

        String clientIp = getClientIp(request);
        String key = RATE_LIMIT_PREFIX + clientIp;

        long now = System.currentTimeMillis();
        long windowStart = windowStarts.getOrDefault(clientIp, now);
        long requestCount = requestCounts.getOrDefault(clientIp, 0L);

        if (now - windowStart > 60000) {
            windowStarts.put(clientIp, now);
            requestCounts.put(clientIp, 1L);
            return chain.filter(exchange);
        }

        if (requestCount >= maxRequestsPerMinute) {
            LOG.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().add("Retry-After", "60");
            return exchange.getResponse().setComplete();
        }

        requestCounts.put(clientIp, requestCount + 1);

        return redisTemplate.opsForValue()
                .increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        return redisTemplate.expire(key, Duration.ofMinutes(1))
                                .thenReturn(count);
                    }
                    return Mono.just(count);
                })
                .then(chain.filter(exchange));
    }

    private boolean shouldRateLimit(String path) {
        if (!rateLimitEnabled) {
            return false;
        }
        for (String limitedPath : LIMITED_PATHS) {
            if (path.contains(limitedPath)) {
                return true;
            }
        }
        return false;
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null 
                ? request.getRemoteAddress().getAddress().getHostAddress() 
                : "unknown";
    }

    public Mono<Boolean> isRateLimited(String clientIp) {
        String key = RATE_LIMIT_PREFIX + clientIp;
        return redisTemplate.opsForValue().get(key)
                .map(count -> Long.parseLong(count) >= maxRequestsPerMinute)
                .defaultIfEmpty(false);
    }

    public Mono<Void> clearRateLimit(String clientIp) {
        String key = RATE_LIMIT_PREFIX + clientIp;
        return redisTemplate.delete(key).then();
    }
}
