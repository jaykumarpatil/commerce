package com.projects.microservices.auth.adapter.outbound.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.projects.microservices.auth.application.port.outbound.AuthEventPublisherPort;
import com.projects.microservices.auth.domain.event.AuthEvent;

@Service
public class AuthEventPublisherAdapter implements AuthEventPublisherPort {

    private static final Logger LOG = LoggerFactory.getLogger(AuthEventPublisherAdapter.class);

    @Override
    public void publish(AuthEvent event) {
        LOG.info("Publishing auth event: {}", event.getClass().getSimpleName());
    }
}
