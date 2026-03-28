package se.magnus.microservices.payment.adapter.outbound.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.magnus.microservices.payment.application.port.outbound.PaymentEventPublisherPort;
import se.magnus.microservices.payment.domain.event.PaymentEvent;

@Service
public class PaymentEventPublisherAdapter implements PaymentEventPublisherPort {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentEventPublisherAdapter.class);

    @Override
    public void publish(PaymentEvent event) {
        LOG.info("Publishing payment event: {}", event.getClass().getSimpleName());
    }
}
