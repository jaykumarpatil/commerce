package com.projects.microservices.auth.application.port.outbound;

import reactor.core.publisher.Mono;

public interface TokenProviderPort {
    String generateAccessToken(String userId, String username, String role);
    String generateRefreshToken(String userId);
    boolean validateToken(String token);
    Mono<Void> revokeToken(String userId);
    Mono<Void> revokeAllUserTokens(String userId);
}
