package com.projects.microservices.core.order.service.adapter.outbound;

import com.projects.api.core.shipping.Shipment;
import com.projects.microservices.core.order.service.port.outbound.ShippingPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ShippingWebClientAdapter implements ShippingPort {

    private final WebClient webClient;

    public ShippingWebClientAdapter(
            WebClient.Builder builder,
            @Value("${app.services.shipping.base-url:http://shipping-service}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<Shipment> getShipmentByOrderId(String orderId) {
        return webClient.get()
                .uri("/v1/shipping/shipments/order/{orderId}", orderId)
                .retrieve()
                .bodyToMono(Shipment.class);
    }

    @Override
    public Mono<Shipment> cancelShipment(String shipmentId) {
        return webClient.patch()
                .uri(uriBuilder -> uriBuilder.path("/v1/shipping/shipments/{shipmentId}/status")
                        .queryParam("status", "CANCELLED")
                        .build(shipmentId))
                .retrieve()
                .bodyToMono(Shipment.class);
    }
}
