package se.magnus.microservices.payment.application.port.outbound;

import se.magnus.microservices.payment.domain.event.PaymentEvent;

public interface PaymentEventPublisherPort {
    void publish(PaymentEvent event);
}
