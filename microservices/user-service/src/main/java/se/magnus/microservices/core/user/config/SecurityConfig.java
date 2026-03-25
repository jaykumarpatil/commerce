package se.magnus.microservices.core.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import se.magnus.microservices.core.user.services.JwtTokenProvider;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/v1/users/register", "/v1/users/login", "/v1/users/password-reset").permitAll()
                        .pathMatchers(HttpMethod.GET, "/v1/users/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/v1/users/**").hasAnyRole("USER", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/v1/users/**").hasAnyRole("USER", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/v1/users/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(new JwtAuthenticationConverter(jwtTokenProvider))
                        )
                );

        return http.build();
    }
}
