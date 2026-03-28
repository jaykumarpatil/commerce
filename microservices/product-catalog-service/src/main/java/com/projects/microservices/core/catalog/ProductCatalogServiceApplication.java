package com.projects.microservices.core.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class ProductCatalogServiceApplication {

    private static final org.slf4j.Logger LOG = 
            org.slf4j.LoggerFactory.getLogger(ProductCatalogServiceApplication.class);

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(ProductCatalogServiceApplication.class, args);
    }
}
