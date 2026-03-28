package se.magnus.microservices.cart.application.port.outbound;

import se.magnus.microservices.cart.domain.event.CartEvent;
import reactor.core.publisher.Flux;

public interface CartEventPublisherPort {
    void publish(CartEvent event);
    Flux<CartEvent> subscribe(String cartId);
}
