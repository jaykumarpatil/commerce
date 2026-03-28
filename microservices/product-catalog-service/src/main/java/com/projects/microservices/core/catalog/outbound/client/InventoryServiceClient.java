package com.projects.microservices.core.catalog.outbound.client;

import com.projects.api.core.inventory.InventoryItem;
import com.projects.microservices.core.catalog.outbound.port.InventoryReadPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class InventoryServiceClient implements InventoryReadPort {

    private final WebClient webClient;

    public InventoryServiceClient(WebClient.Builder webClientBuilder,
                                  @Value("${app.clients.inventory-service.base-url:http://inventory-service}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<InventoryItem> getInventoryByProductId(String productId) {
        return webClient.get()
                .uri("/v1/inventory/product/{productId}", productId)
                .retrieve()
                .bodyToMono(InventoryItem.class);
    }
}
