package com.projects.microservices.core.order.service.adapter.outbound;

import com.projects.api.core.notification.Notification;
import com.projects.microservices.core.order.service.port.outbound.NotificationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class NotificationWebClientAdapter implements NotificationPort {

    private final WebClient webClient;

    public NotificationWebClientAdapter(
            WebClient.Builder builder,
            @Value("${app.services.notification.base-url:http://notification-service}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<Notification> sendOrderConfirmation(Notification notification) {
        return webClient.post()
                .uri("/v1/notifications")
                .bodyValue(notification)
                .retrieve()
                .bodyToMono(Notification.class);
    }
}
