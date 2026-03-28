package com.projects.microservices.core.inventory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.projects.api.core.inventory.InventoryItem;
import com.projects.api.core.inventory.InventoryService;
import com.projects.api.core.inventory.StockReservation;
import com.projects.api.exceptions.BadRequestException;
import com.projects.api.exceptions.NotFoundException;
import com.projects.microservices.core.inventory.persistence.*;
import com.projects.microservices.core.inventory.service.port.outbound.LowStockAlertNotificationRequest;
import com.projects.microservices.core.inventory.service.port.outbound.NotificationClientPort;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository reservationRepository;
    private final InventoryMapper inventoryMapper;
    private final StockReservationMapper reservationMapper;
    private final NotificationClientPort notificationClientPort;
    private final Map<String, LowStockAlertState> lowStockAlertStates = new ConcurrentHashMap<>();

    @Value("${inventory.low-stock-threshold:10}")
    private int lowStockThreshold;

    @Value("${inventory.low-stock-alert.throttle-minutes:60}")
    private int lowStockAlertThrottleMinutes;

    @Value("${inventory.low-stock-alert.recipient:inventory-ops}")
    private String lowStockAlertRecipient;

    @Value("${inventory.reservation.timeout-minutes:15}")
    private int reservationTimeoutMinutes;

    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                               StockReservationRepository reservationRepository,
                               InventoryMapper inventoryMapper,
                               StockReservationMapper reservationMapper,
                               NotificationClientPort notificationClientPort) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.inventoryMapper = inventoryMapper;
        this.reservationMapper = reservationMapper;
        this.notificationClientPort = notificationClientPort;
    }

    @Override
    public Flux<InventoryItem> getAllInventory() {
        return inventoryRepository.findAll()
                .map(inventoryMapper::entityToApi);
    }

    @Override
    public Mono<InventoryItem> getInventoryByProductId(String productId) {
        return inventoryRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found for product: " + productId)))
                .map(inventoryMapper::entityToApi);
    }

    @Override
    public Mono<StockReservation> reserveStock(String productId, Integer quantity) {
        validatePositiveQuantity(quantity, "reserve");
        return inventoryRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found for product: " + productId)))
                .flatMap(entity -> reserveFromEntity(entity, quantity));
    }

    @Override
    public Mono<StockReservation> confirmReservation(String reservationId) {
        return reservationRepository.findByReservationId(reservationId)
                .switchIfEmpty(Mono.error(new NotFoundException("Reservation not found: " + reservationId)))
                .flatMap(this::confirmReservationEntity);
    }

    @Override
    public Mono<StockReservation> cancelReservation(String reservationId) {
        return reservationRepository.findByReservationId(reservationId)
                .switchIfEmpty(Mono.error(new NotFoundException("Reservation not found: " + reservationId)))
                .flatMap(entity -> {
                    if (!"PENDING".equals(entity.getStatus())) {
                        return Mono.error(new BadRequestException("Reservation already " + entity.getStatus()));
                    }
                    return releaseReservation(entity);
                });
    }

    @Override
    public Mono<InventoryItem> adjustStock(String productId, Integer quantity) {
        validatePositiveQuantity(quantity, "adjust");
        return inventoryRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found for product: " + productId)))
                .flatMap(entity -> adjustInventory(entity, quantity));
    }

    @Override
    public Mono<Void> releaseReservedStock(String productId, Integer quantity) {
        validatePositiveQuantity(quantity, "release");
        return inventoryRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found for product: " + productId)))
                .flatMap(entity -> {
                    if (entity.getReservedQuantity() < quantity) {
                        return Mono.error(new BadRequestException("Insufficient reserved stock"));
                    }
                    entity.setReservedQuantity(entity.getReservedQuantity() - quantity);
                    entity.setAvailableQuantity(entity.getAvailableQuantity() + quantity);
                    return inventoryRepository.save(entity).then();
                });
    }

    @Override
    public Flux<StockReservation> getReservationsByOrderId(String orderId) {
        return reservationRepository.findByOrderId(orderId)
                .map(reservationMapper::entityToApi);
    }

    public Flux<InventoryItem> getLowStockItems() {
        return inventoryRepository.findAll()
                .filter(entity -> entity.getAvailableQuantity() <= lowStockThreshold)
                .map(inventoryMapper::entityToApi);
    }

    @Scheduled(fixedRate = 60000)
    public void processExpiredReservations() {
    }

    @Scheduled(fixedRate = 300000)
    public void checkLowStockAlerts() {
        Instant schedulerRunAt = Instant.now();
        getLowStockItems()
                .filter(item -> item.getAvailableQuantity() <= lowStockThreshold)
                .flatMap(item -> sendLowStockAlertIfNeeded(item, schedulerRunAt))
                .subscribe();
    }

    private Mono<Void> sendLowStockAlertIfNeeded(InventoryItem item, Instant timestamp) {
        String productId = item.getProductId();
        Integer availableQuantity = item.getAvailableQuantity();

        LowStockAlertState previousAlertState = lowStockAlertStates.get(productId);
        boolean quantityChanged = previousAlertState == null
            || !Objects.equals(previousAlertState.availableQuantity(), availableQuantity);
        boolean throttleWindowElapsed = previousAlertState == null
            || Duration.between(previousAlertState.lastSentAt(), timestamp).toMinutes() >= lowStockAlertThrottleMinutes;

        if (!quantityChanged && !throttleWindowElapsed) {
            return Mono.empty();
        }

        LowStockAlertNotificationRequest request = new LowStockAlertNotificationRequest(
            productId,
            availableQuantity,
            lowStockThreshold,
            timestamp,
            lowStockAlertRecipient
        );

        return notificationClientPort.sendLowStockAlert(request)
            .doOnSuccess(ignored -> {
                lowStockAlertStates.put(productId, new LowStockAlertState(availableQuantity, timestamp));
                LOG.warn(
                    "LOW_STOCK_ALERT sent: productId={}, availableQuantity={}, threshold={}, timestamp={}",
                    productId,
                    availableQuantity,
                    lowStockThreshold,
                    timestamp
                );
            })
            .doOnError(error -> LOG.error(
                "LOW_STOCK_ALERT failed: productId={}, availableQuantity={}, threshold={}, timestamp={}",
                productId,
                availableQuantity,
                lowStockThreshold,
                timestamp,
                error
            ))
            .onErrorResume(error -> Mono.empty());
    }

    private record LowStockAlertState(Integer availableQuantity, Instant lastSentAt) {}

    private Mono<StockReservation> reserveFromEntity(InventoryEntity entity, int quantity) {
        if (entity.getAvailableQuantity() < quantity) {
            return Mono.error(new BadRequestException("Insufficient stock"));
        }
        
        entity.setAvailableQuantity(entity.getAvailableQuantity() - quantity);
        entity.setReservedQuantity(entity.getReservedQuantity() + quantity);
        
        StockReservationEntity reservation = new StockReservationEntity();
        reservation.setReservationId(UUID.randomUUID().toString());
        reservation.setProductId(entity.getProductId());
        reservation.setQuantity(quantity);
        reservation.setStatus("PENDING");
        reservation.setReservedAt(java.time.LocalDateTime.now());
        
        return Mono.zip(inventoryRepository.save(entity), reservationRepository.save(reservation))
                .map(tuple -> reservationMapper.entityToApi(tuple.getT2()));
    }

    private Mono<StockReservation> confirmReservationEntity(StockReservationEntity entity) {
        if (!"PENDING".equals(entity.getStatus())) {
            return Mono.error(new BadRequestException("Reservation already " + entity.getStatus()));
        }
        
        entity.setStatus("CONFIRMED");
        entity.setConfirmedAt(java.time.LocalDateTime.now());
        
        return reservationRepository.save(entity)
                .map(reservationMapper::entityToApi);
    }

    private Mono<StockReservation> releaseReservation(StockReservationEntity entity) {
        return inventoryRepository.findByProductId(entity.getProductId())
                .flatMap(inventory -> {
                    inventory.setReservedQuantity(inventory.getReservedQuantity() - entity.getQuantity());
                    inventory.setAvailableQuantity(inventory.getAvailableQuantity() + entity.getQuantity());
                    return inventoryRepository.save(inventory);
                })
                .then(Mono.fromRunnable(() -> {
                    entity.setStatus("CANCELLED");
                    entity.setCancelledAt(java.time.LocalDateTime.now());
                }))
                .then(reservationRepository.save(entity))
                .map(reservationMapper::entityToApi);
    }

    private Mono<InventoryItem> adjustInventory(InventoryEntity entity, int quantity) {
        entity.setTotalQuantity(entity.getTotalQuantity() + quantity);
        entity.setAvailableQuantity(entity.getAvailableQuantity() + quantity);
        return inventoryRepository.save(entity).map(inventoryMapper::entityToApi);
    }

    private void validatePositiveQuantity(Integer quantity, String operation) {
        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than zero for " + operation + " operation");
        }
    }
}
