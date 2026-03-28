package com.projects.microservices.core.order.service.port.outbound;

import com.projects.api.core.shipping.Shipment;
import reactor.core.publisher.Mono;

public interface ShippingPort {
    Mono<Shipment> getShipmentByOrderId(String orderId);
    Mono<Shipment> cancelShipment(String shipmentId);
}
