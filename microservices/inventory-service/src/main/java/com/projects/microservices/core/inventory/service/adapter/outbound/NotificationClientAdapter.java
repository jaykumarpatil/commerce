package com.projects.microservices.core.inventory.service.adapter.outbound;

import com.projects.api.core.notification.Notification;
import com.projects.api.core.notification.NotificationChannel;
import com.projects.api.core.notification.NotificationType;
import com.projects.microservices.core.inventory.service.port.outbound.LowStockAlertNotificationRequest;
import com.projects.microservices.core.inventory.service.port.outbound.NotificationClientPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class NotificationClientAdapter implements NotificationClientPort {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationClientAdapter.class);

    private final WebClient webClient;

    public NotificationClientAdapter(
        WebClient.Builder webClientBuilder,
        @Value("${app.notification-service.url:http://notification-service}") String notificationServiceBaseUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(notificationServiceBaseUrl).build();
    }

    @Override
    @Retry(name = "notificationClient", fallbackMethod = "sendLowStockAlertFallback")
    @CircuitBreaker(name = "notificationClient", fallbackMethod = "sendLowStockAlertFallback")
    public Mono<Void> sendLowStockAlert(LowStockAlertNotificationRequest request) {
        Notification notificationPayload = buildNotificationPayload(request);

        return webClient.post()
            .uri("/v1/notifications")
            .bodyValue(notificationPayload)
            .retrieve()
            .bodyToMono(Notification.class)
            .doOnNext(response -> LOG.debug("LOW_STOCK_ALERT notification accepted. notificationId={}", response.getNotificationId()))
            .then();
    }

    private Mono<Void> sendLowStockAlertFallback(LowStockAlertNotificationRequest request, Throwable throwable) {
        LOG.error(
            "Failed to send LOW_STOCK_ALERT for productId={} after retry/circuit-breaker handling. reason={}",
            request.productId(),
            throwable.toString()
        );
        return Mono.error(throwable);
    }

    private Notification buildNotificationPayload(LowStockAlertNotificationRequest request) {
        String context = Map.of(
            "productId", request.productId(),
            "availableQuantity", request.availableQuantity(),
            "threshold", request.threshold(),
            "timestamp", request.timestamp().toString()
        ).toString();

        Notification notification = new Notification();
        notification.setUserId("inventory-service");
        notification.setType(NotificationType.LOW_STOCK_ALERT);
        notification.setChannel(NotificationChannel.SYSTEM);
        notification.setSubject("Low stock alert: " + request.productId());
        notification.setMessage(context);
        notification.setRecipient(request.recipient());
        notification.setIsRead(false);
        notification.setStatus("PENDING");
        notification.setCreatedAt(request.timestamp().toString());
        return notification;
    }
}
