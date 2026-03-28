package com.projects.microservices.core.order.service.adapter.outbound;

import com.projects.api.core.inventory.StockReservation;
import com.projects.microservices.core.order.service.port.outbound.InventoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class InventoryWebClientAdapter implements InventoryPort {

    private final WebClient webClient;

    public InventoryWebClientAdapter(
            WebClient.Builder builder,
            @Value("${app.services.inventory.base-url:http://inventory-service}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<StockReservation> reserveStock(String productId, Integer quantity) {
        return webClient.patch()
                .uri(uriBuilder -> uriBuilder.path("/v1/inventory/product/{productId}/reserve")
                        .queryParam("quantity", quantity)
                        .build(productId))
                .retrieve()
                .bodyToMono(StockReservation.class);
    }

    @Override
    public Mono<Void> releaseReservedStock(String productId, Integer quantity) {
        return webClient.patch()
                .uri(uriBuilder -> uriBuilder.path("/v1/inventory/product/{productId}/release")
                        .queryParam("quantity", quantity)
                        .build(productId))
                .retrieve()
                .bodyToMono(Void.class);
    }

    @Override
    public Flux<StockReservation> getReservationsByOrderId(String orderId) {
        return webClient.get()
                .uri("/v1/inventory/reservations/order/{orderId}", orderId)
                .retrieve()
                .bodyToFlux(StockReservation.class);
    }

    @Override
    public Mono<StockReservation> cancelReservation(String reservationId) {
        return webClient.patch()
                .uri("/v1/inventory/reservation/{reservationId}/cancel", reservationId)
                .retrieve()
                .bodyToMono(StockReservation.class);
    }
}
