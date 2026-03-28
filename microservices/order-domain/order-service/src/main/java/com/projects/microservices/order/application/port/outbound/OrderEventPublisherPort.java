package com.projects.microservices.order.application.port.outbound;

import com.projects.microservices.order.domain.event.OrderEvent;
import reactor.core.publisher.Flux;

public interface OrderEventPublisherPort {
    void publish(OrderEvent event);
    Flux<OrderEvent> subscribe(String orderId);
}
