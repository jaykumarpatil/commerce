package se.magnus.microservices.auth.application.port.outbound;

import se.magnus.microservices.auth.domain.event.AuthEvent;

public interface AuthEventPublisherPort {
    void publish(AuthEvent event);
}
