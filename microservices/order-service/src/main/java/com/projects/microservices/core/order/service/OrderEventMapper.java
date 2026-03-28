package com.projects.microservices.core.order.service;

import com.projects.api.core.order.OrderEvent;
import com.projects.api.core.order.OrderStatus;
import com.projects.microservices.core.order.persistence.OrderEventEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderEventMapper {
    OrderEvent entityToApi(OrderEventEntity entity);
    OrderEventEntity apiToEntity(OrderEvent event);

    default OrderStatus map(String status) {
        return status == null ? null : OrderStatus.valueOf(status);
    }

    default String map(OrderStatus status) {
        return status == null ? null : status.name();
    }
}
