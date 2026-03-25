package se.magnus.microservices.core.shipping.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.shipping.*;
import se.magnus.util.http.ServiceUtil;

@RestController
public class ShippingController implements ShippingService {

    private final ShippingService shippingService;
    private final ServiceUtil serviceUtil;

    @Autowired
    public ShippingController(ShippingService shippingService, ServiceUtil serviceUtil) {
        this.shippingService = shippingService;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<ShippingAddress> createAddress(ShippingAddress address) {
        return shippingService.createAddress(address);
    }

    @Override
    public Flux<ShippingAddress> getAddressesByUserId(String userId) {
        return shippingService.getAddressesByUserId(userId);
    }

    @Override
    public Mono<ShippingRate> calculateShippingRate(ShippingRate rate) {
        return shippingService.calculateShippingRate(rate);
    }

    @Override
    public Mono<Shipment> createShipment(Shipment shipment) {
        return shippingService.createShipment(shipment);
    }

    @Override
    public Mono<Shipment> getShipment(String shipmentId) {
        return shippingService.getShipment(shipmentId);
    }

    @Override
    public Mono<Shipment> getShipmentByOrderId(String orderId) {
        return shippingService.getShipmentByOrderId(orderId);
    }

    @Override
    public Mono<Shipment> updateShipmentStatus(String shipmentId, String status) {
        return shippingService.updateShipmentStatus(shipmentId, status);
    }

    @Override
    public Mono<Shipment> updateTrackingNumber(String shipmentId, String trackingNumber) {
        return shippingService.updateTrackingNumber(shipmentId, trackingNumber);
    }

    @Override
    public Mono<Shipment> getShipmentByTracking(String trackingNumber) {
        return shippingService.getShipmentByTracking(trackingNumber);
    }
}
