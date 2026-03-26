package se.magnus.microservices.core.shipping.service;

import static java.util.logging.Level.FINE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.shipping.*;
import se.magnus.api.exceptions.BadRequestException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.microservices.core.shipping.persistence.*;

@Service
public class ShippingServiceImpl implements ShippingService {

    private static final Logger LOG = LoggerFactory.getLogger(ShippingServiceImpl.class);

    private final ShippingAddressRepository addressRepository;
    private final ShippingRateRepository rateRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShippingAddressMapper addressMapper;
    private final ShippingRateMapper rateMapper;
    private final ShipmentMapper shipmentMapper;

    @Autowired
    public ShippingServiceImpl(ShippingAddressRepository addressRepository,
                              ShippingRateRepository rateRepository,
                              ShipmentRepository shipmentRepository,
                              ShippingAddressMapper addressMapper,
                              ShippingRateMapper rateMapper,
                              ShipmentMapper shipmentMapper) {
        this.addressRepository = addressRepository;
        this.rateRepository = rateRepository;
        this.shipmentRepository = shipmentRepository;
        this.addressMapper = addressMapper;
        this.rateMapper = rateMapper;
        this.shipmentMapper = shipmentMapper;
    }

    @Override
    public Mono<ShippingAddress> createAddress(ShippingAddress address) {
        if (address.getUserId() == null || address.getUserId().isEmpty()) {
            return Mono.error(new BadRequestException("User ID is required"));
        }

        ShippingAddressEntity entity = addressMapper.apiToEntity(address);
        
        return addressRepository.save(entity)
                .log(LOG.getName(), FINE)
                .map(addressMapper::entityToApi);
    }

    @Override
    public Flux<ShippingAddress> getAddressesByUserId(String userId) {
        return addressRepository.findByUserId(userId)
                .map(addressMapper::entityToApi);
    }

    @Override
    public Mono<ShippingRate> calculateShippingRate(ShippingRate rate) {
        // Simple shipping rate calculation
        double basePrice = 9.99;
        double weightPrice = rate.getWeight() != null ? rate.getWeight() * 0.5 : 0;
        double distancePrice = 14.99; // Flat rate for now
        
        double totalAmount = basePrice + weightPrice + distancePrice;
        
        rate.setBasePrice(basePrice);
        rate.setWeightPrice(weightPrice);
        rate.setDistancePrice(distancePrice);
        rate.setTotalAmount(totalAmount);
        rate.setEstimatedDeliveryDays(5); // 5 business days
        rate.setEstimatedDeliveryDate(java.time.LocalDate.now().plusDays(5).toString());
        
        return Mono.just(rate);
    }

    @Override
    public Mono<Shipment> createShipment(Shipment shipment) {
        if (shipment.getOrderId() == null || shipment.getOrderId().isEmpty()) {
            return Mono.error(new BadRequestException("Order ID is required"));
        }

        ShipmentEntity entity = shipmentMapper.apiToEntity(shipment);
        
        return shipmentRepository.save(entity)
                .log(LOG.getName(), FINE)
                .map(shipmentMapper::entityToApi);
    }

    @Override
    public Mono<Shipment> getShipment(String shipmentId) {
        return shipmentRepository.findByShipmentId(shipmentId)
                .switchIfEmpty(Mono.error(new NotFoundException("Shipment not found: " + shipmentId)))
                .map(shipmentMapper::entityToApi);
    }

    @Override
    public Mono<Shipment> getShipmentByOrderId(String orderId) {
        return shipmentRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Shipment not found for order: " + orderId)))
                .map(shipmentMapper::entityToApi);
    }

    @Override
    public Mono<Shipment> updateShipmentStatus(String shipmentId, String status) {
        return shipmentRepository.findByShipmentId(shipmentId)
                .switchIfEmpty(Mono.error(new NotFoundException("Shipment not found: " + shipmentId)))
                .flatMap(entity -> {
                    entity.setStatus(status);
                    
                    if ("DELIVERED".equals(status)) {
                        entity.setActualDeliveryDate(java.time.LocalDateTime.now());
                    }
                    
                    return shipmentRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(shipmentMapper::entityToApi);
                });
    }

    @Override
    public Mono<Shipment> updateTrackingNumber(String shipmentId, String trackingNumber) {
        return shipmentRepository.findByShipmentId(shipmentId)
                .switchIfEmpty(Mono.error(new NotFoundException("Shipment not found: " + shipmentId)))
                .flatMap(entity -> {
                    entity.setTrackingNumber(trackingNumber);
                    return shipmentRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(shipmentMapper::entityToApi);
                });
    }

    @Override
    public Mono<Shipment> getShipmentByTracking(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber)
                .switchIfEmpty(Mono.error(new NotFoundException("Shipment not found with tracking: " + trackingNumber)))
                .map(shipmentMapper::entityToApi);
    }
}
