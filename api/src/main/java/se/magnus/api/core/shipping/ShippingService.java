package se.magnus.api.core.shipping;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ShippingService {

    @PostMapping("/v1/shipping/addresses")
    Mono<ShippingAddress> createAddress(ShippingAddress address);

    @GetMapping("/v1/shipping/addresses/user/{userId}")
    Flux<ShippingAddress> getAddressesByUserId(String userId);

    @GetMapping("/v1/shipping/rates")
    Mono<ShippingRate> calculateShippingRate(ShippingRate rate);

    @PostMapping("/v1/shipping/shipments")
    Mono<Shipment> createShipment(Shipment shipment);

    @GetMapping("/v1/shipping/shipments/{shipmentId}")
    Mono<Shipment> getShipment(String shipmentId);

    @GetMapping("/v1/shipping/shipments/order/{orderId}")
    Mono<Shipment> getShipmentByOrderId(String orderId);

    @PatchMapping("/v1/shipping/shipments/{shipmentId}/status")
    Mono<Shipment> updateShipmentStatus(String shipmentId, String status);

    @PatchMapping("/v1/shipping/shipments/{shipmentId}/tracking")
    Mono<Shipment> updateTrackingNumber(String shipmentId, String trackingNumber);

    @GetMapping("/v1/shipping/shipments/tracking/{trackingNumber}")
    Mono<Shipment> getShipmentByTracking(String trackingNumber);
}
