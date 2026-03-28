package com.projects.microservices.payment.application.port.outbound;

import com.projects.microservices.payment.domain.event.PaymentEvent;

public interface PaymentEventPublisherPort {
    void publish(PaymentEvent event);
}
