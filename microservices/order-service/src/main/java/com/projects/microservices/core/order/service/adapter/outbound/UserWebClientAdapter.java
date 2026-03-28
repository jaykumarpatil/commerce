package com.projects.microservices.core.order.service.adapter.outbound;

import com.projects.api.core.user.User;
import com.projects.microservices.core.order.service.port.outbound.UserPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserWebClientAdapter implements UserPort {

    private final WebClient webClient;

    public UserWebClientAdapter(
            WebClient.Builder builder,
            @Value("${app.services.user.base-url:http://user-service}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<User> getUserById(String userId) {
        return webClient.get()
                .uri("/v1/users/{userId}", userId)
                .retrieve()
                .bodyToMono(User.class);
    }
}
