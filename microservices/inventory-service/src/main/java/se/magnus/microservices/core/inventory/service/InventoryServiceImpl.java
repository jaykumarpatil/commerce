package se.magnus.microservices.core.inventory.service;

import static java.util.logging.Level.FINE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.inventory.*;
import se.magnus.api.exceptions.BadRequestException;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.microservices.core.inventory.persistence.*;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository reservationRepository;
    private final InventoryMapper inventoryMapper;
    private final StockReservationMapper reservationMapper;

    @Autowired
    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                               StockReservationRepository reservationRepository,
                               InventoryMapper inventoryMapper,
                               StockReservationMapper reservationMapper) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.inventoryMapper = inventoryMapper;
        this.reservationMapper = reservationMapper;
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
        return inventoryRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found for product: " + productId)))
                .flatMap(entity -> {
                    if (entity.getAvailableQuantity() < quantity) {
                        return Mono.error(new BadRequestException("Insufficient stock. Available: " + entity.getAvailableQuantity() + ", Requested: " + quantity));
                    }
                    
                    // Update inventory
                    entity.setAvailableQuantity(entity.getAvailableQuantity() - quantity);
                    entity.setReservedQuantity(entity.getReservedQuantity() + quantity);
                    
                    // Create reservation
                    StockReservationEntity reservation = new StockReservationEntity();
                    reservation.setReservationId(java.util.UUID.randomUUID().toString());
                    reservation.setOrderId(null); // Will be set when order is confirmed
                    reservation.setProductId(productId);
                    reservation.setQuantity(quantity);
                    reservation.setStatus("PENDING");
                    reservation.setReservedAt(java.time.LocalDateTime.now());
                    
                    return Mono.zip(
                            inventoryRepository.save(entity),
                            reservationRepository.save(reservation)
                    ).map(tuple -> {
                        entity = tuple.getT1();
                        reservation = tuple.getT2();
                        return reservationMapper.entityToApi(reservation);
                    });
                })
                .log(LOG.getName(), FINE);
    }

    @Override
    public Mono<StockReservation> confirmReservation(String reservationId) {
        return reservationRepository.findByReservationId(reservationId)
                .switchIfEmpty(Mono.error(new NotFoundException("Reservation not found: " + reservationId)))
                .flatMap(entity -> {
                    if (!"PENDING".equals(entity.getStatus())) {
                        return Mono.error(new BadRequestException("Reservation already " + entity.getStatus()));
                    }
                    
                    entity.setStatus("CONFIRMED");
                    entity.setConfirmedAt(java.time.LocalDateTime.now());
                    
                    return reservationRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(reservationMapper::entityToApi);
                });
    }

    @Override
    public Mono<StockReservation> cancelReservation(String reservationId) {
        return reservationRepository.findByReservationId(reservationId)
                .switchIfEmpty(Mono.error(new NotFoundException("Reservation not found: " + reservationId)))
                .flatMap(entity -> {
                    if (!"PENDING".equals(entity.getStatus())) {
                        return Mono.error(new BadRequestException("Reservation already " + entity.getStatus()));
                    }
                    
                    // Release reserved stock
                    return inventoryRepository.findByProductId(entity.getProductId())
                            .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found for product: " + entity.getProductId())))
                            .flatMap(inventoryEntity -> {
                                inventoryEntity.setReservedQuantity(inventoryEntity.getReservedQuantity() - entity.getQuantity());
                                inventoryEntity.setAvailableQuantity(inventoryEntity.getAvailableQuantity() + entity.getQuantity());
                                
                                entity.setStatus("CANCELLED");
                                entity.setCancelledAt(java.time.LocalDateTime.now());
                                
                                return Mono.zip(
                                        inventoryRepository.save(inventoryEntity),
                                        reservationRepository.save(entity)
                                ).map(tuple -> reservationMapper.entityToApi(entity));
                            });
                })
                .log(LOG.getName(), FINE);
    }

    @Override
    public Mono<InventoryItem> adjustStock(String productId, Integer quantity) {
        return inventoryRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found for product: " + productId)))
                .flatMap(entity -> {
                    entity.setTotalQuantity(entity.getTotalQuantity() + quantity);
                    entity.setAvailableQuantity(entity.getAvailableQuantity() + quantity);
                    
                    return inventoryRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(inventoryMapper::entityToApi);
                });
    }

    @Override
    public Mono<Void> releaseReservedStock(String productId, Integer quantity) {
        return inventoryRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found for product: " + productId)))
                .flatMap(entity -> {
                    if (entity.getReservedQuantity() < quantity) {
                        return Mono.error(new BadRequestException("Insufficient reserved stock. Reserved: " + entity.getReservedQuantity() + ", Release: " + quantity));
                    }
                    
                    entity.setReservedQuantity(entity.getReservedQuantity() - quantity);
                    entity.setAvailableQuantity(entity.getAvailableQuantity() + quantity);
                    
                    return inventoryRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .then(Mono.empty());
                });
    }

    @Override
    public Flux<StockReservation> getReservationsByOrderId(String orderId) {
        return reservationRepository.findByOrderId(orderId)
                .map(reservationMapper::entityToApi);
    }
}
