package se.magnus.microservices.core.inventory.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.inventory.*;
import se.magnus.util.http.ServiceUtil;

@RestController
public class InventoryController implements InventoryService {

    private final InventoryService inventoryService;
    private final ServiceUtil serviceUtil;

    @Autowired
    public InventoryController(InventoryService inventoryService, ServiceUtil serviceUtil) {
        this.inventoryService = inventoryService;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Flux<InventoryItem> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    @Override
    public Mono<InventoryItem> getInventoryByProductId(String productId) {
        return inventoryService.getInventoryByProductId(productId);
    }

    @Override
    public Mono<StockReservation> reserveStock(String productId, Integer quantity) {
        return inventoryService.reserveStock(productId, quantity);
    }

    @Override
    public Mono<StockReservation> confirmReservation(String reservationId) {
        return inventoryService.confirmReservation(reservationId);
    }

    @Override
    public Mono<StockReservation> cancelReservation(String reservationId) {
        return inventoryService.cancelReservation(reservationId);
    }

    @Override
    public Mono<InventoryItem> adjustStock(String productId, Integer quantity) {
        return inventoryService.adjustStock(productId, quantity);
    }

    @Override
    public Mono<Void> releaseReservedStock(String productId, Integer quantity) {
        return inventoryService.releaseReservedStock(productId, quantity);
    }

    @Override
    public Flux<StockReservation> getReservationsByOrderId(String orderId) {
        return inventoryService.getReservationsByOrderId(orderId);
    }
}
