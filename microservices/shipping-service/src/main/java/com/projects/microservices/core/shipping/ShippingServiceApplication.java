package com.projects.microservices.core.shipping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class ShippingServiceApplication {

    private static final org.slf4j.Logger LOG = 
            org.slf4j.LoggerFactory.getLogger(ShippingServiceApplication.class);

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(ShippingServiceApplication.class, args);
    }
}
