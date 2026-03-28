package com.projects.microservices.core.order.service;

import com.projects.api.core.order.Order;
import com.projects.api.core.order.OrderStatus;
import com.projects.microservices.core.order.persistence.OrderEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order entityToApi(OrderEntity entity);
    OrderEntity apiToEntity(Order order);

    default OrderStatus map(String status) {
        return status == null ? null : OrderStatus.valueOf(status);
    }

    default String map(OrderStatus status) {
        return status == null ? null : status.name();
    }
}
