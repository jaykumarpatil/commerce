package com.projects.microservices.core.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class UserApplication {

    private static final org.slf4j.Logger LOG = 
            org.slf4j.LoggerFactory.getLogger(UserApplication.class);

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(UserApplication.class, args);
    }
}
