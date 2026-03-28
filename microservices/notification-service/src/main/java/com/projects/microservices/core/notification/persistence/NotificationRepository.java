package com.projects.microservices.core.notification.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationRepository extends ReactiveCrudRepository<NotificationEntity, Long> {
    Mono<NotificationEntity> findByNotificationId(String notificationId);
    Flux<NotificationEntity> findByUserId(String userId);
}
