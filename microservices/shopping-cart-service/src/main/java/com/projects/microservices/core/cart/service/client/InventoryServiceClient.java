package com.projects.microservices.core.cart.service.client;

import com.projects.api.core.inventory.InventoryItem;
import com.projects.api.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class InventoryServiceClient {

    private static final String INVENTORY_SERVICE_URL = "http://inventory-service";

    private final WebClient webClient;

    @Autowired
    public InventoryServiceClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<InventoryItem> getInventoryByProductId(String productId) {
        return webClient.get()
                .uri(INVENTORY_SERVICE_URL + "/v1/inventory/product/{productId}", productId)
                .retrieve()
                .bodyToMono(InventoryItem.class)
                .onErrorMap(WebClientResponseException.NotFound.class,
                        ex -> new NotFoundException("Inventory not found for product: " + productId));
    }
}
