package com.projects.microservices.core.notification.service;

import org.mapstruct.Mapper;
import com.projects.api.core.notification.Notification;
import com.projects.microservices.core.notification.persistence.NotificationEntity;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    Notification entityToApi(NotificationEntity entity);
    NotificationEntity apiToEntity(Notification notification);
}
