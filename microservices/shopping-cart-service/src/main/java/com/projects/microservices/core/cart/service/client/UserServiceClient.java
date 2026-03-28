package com.projects.microservices.core.cart.service.client;

import com.projects.api.core.user.User;
import com.projects.api.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class UserServiceClient {

    private static final String USER_SERVICE_URL = "http://user-service";

    private final WebClient webClient;

    @Autowired
    public UserServiceClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<User> getUser(String userId) {
        return webClient.get()
                .uri(USER_SERVICE_URL + "/v1/users/{userId}", userId)
                .retrieve()
                .bodyToMono(User.class)
                .onErrorMap(WebClientResponseException.NotFound.class,
                        ex -> new NotFoundException("User not found: " + userId));
    }
}
