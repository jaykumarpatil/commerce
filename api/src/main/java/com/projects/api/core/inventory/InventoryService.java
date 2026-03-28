package com.projects.api.core.inventory;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InventoryService {

    @GetMapping("/v1/inventory")
    Flux<InventoryItem> getAllInventory();

    @GetMapping("/v1/inventory/product/{productId}")
    Mono<InventoryItem> getInventoryByProductId(String productId);

    @PatchMapping("/v1/inventory/product/{productId}/reserve")
    Mono<StockReservation> reserveStock(String productId, Integer quantity);

    @PatchMapping("/v1/inventory/reservation/{reservationId}/confirm")
    Mono<StockReservation> confirmReservation(String reservationId);

    @PatchMapping("/v1/inventory/reservation/{reservationId}/cancel")
    Mono<StockReservation> cancelReservation(String reservationId);

    @PatchMapping("/v1/inventory/product/{productId}/adjust")
    Mono<InventoryItem> adjustStock(String productId, Integer quantity);

    @PatchMapping("/v1/inventory/product/{productId}/release")
    Mono<Void> releaseReservedStock(String productId, Integer quantity);

    @GetMapping("/v1/inventory/reservations/order/{orderId}")
    Flux<StockReservation> getReservationsByOrderId(String orderId);
}
