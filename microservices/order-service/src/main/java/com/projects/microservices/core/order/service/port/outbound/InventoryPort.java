package com.projects.microservices.core.order.service.port.outbound;

import com.projects.api.core.inventory.StockReservation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InventoryPort {
    Mono<StockReservation> reserveStock(String productId, Integer quantity);
    Mono<Void> releaseReservedStock(String productId, Integer quantity);
    Flux<StockReservation> getReservationsByOrderId(String orderId);
    Mono<StockReservation> cancelReservation(String reservationId);
}
