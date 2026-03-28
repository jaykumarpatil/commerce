package com.projects.microservices.core.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@EnableScheduling
public class ShoppingCartServiceApplication {

    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(ShoppingCartServiceApplication.class);

    @Bean
    @LoadBalanced
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(ShoppingCartServiceApplication.class, args);
    }
}
