package com.projects.microservices.core.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class ShoppingCartServiceApplication {

    private static final org.slf4j.Logger LOG = 
            org.slf4j.LoggerFactory.getLogger(ShoppingCartServiceApplication.class);

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(ShoppingCartServiceApplication.class, args);
    }
}
