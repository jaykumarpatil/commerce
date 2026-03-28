package se.magnus.microservices.order.adapter.outbound.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.magnus.microservices.order.application.port.outbound.OrderEventPublisherPort;
import se.magnus.microservices.order.domain.event.OrderEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class OrderEventPublisherAdapter implements OrderEventPublisherPort {

    private static final Logger LOG = LoggerFactory.getLogger(OrderEventPublisherAdapter.class);
    
    @Override
    public void publish(OrderEvent event) {
        LOG.info("Publishing order event: {} for order {}", event.getClass().getSimpleName(), 
                extractOrderId(event));
    }

    @Override
    public Flux<OrderEvent> subscribe(String orderId) {
        return Flux.empty();
    }
    
    private String extractOrderId(OrderEvent event) {
        return switch (event) {
            case OrderEvent.Created e -> e.orderId();
            case OrderEvent.StatusChanged e -> e.orderId();
            case OrderEvent.PaymentReceived e -> e.orderId();
            case OrderEvent.Shipped e -> e.orderId();
            case OrderEvent.Delivered e -> e.orderId();
            case OrderEvent.Cancelled e -> e.orderId();
        };
    }
}
