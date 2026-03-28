package com.projects.microservices.auth.application.port.outbound;

import com.projects.microservices.auth.domain.event.AuthEvent;

public interface AuthEventPublisherPort {
    void publish(AuthEvent event);
}
