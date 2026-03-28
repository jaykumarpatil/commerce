package se.magnus.microservices.order.application.port.outbound;

import se.magnus.microservices.order.domain.event.OrderEvent;
import reactor.core.publisher.Flux;

public interface OrderEventPublisherPort {
    void publish(OrderEvent event);
    Flux<OrderEvent> subscribe(String orderId);
}
