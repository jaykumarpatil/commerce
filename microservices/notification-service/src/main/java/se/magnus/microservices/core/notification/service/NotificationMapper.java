package se.magnus.microservices.core.notification.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.notification.Notification;
import se.magnus.microservices.core.notification.persistence.NotificationEntity;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    Notification entityToApi(NotificationEntity entity);
    NotificationEntity apiToEntity(Notification notification);
}
