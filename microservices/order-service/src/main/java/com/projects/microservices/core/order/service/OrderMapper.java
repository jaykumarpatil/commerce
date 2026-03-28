package com.projects.microservices.core.order.service;

import org.mapstruct.Mapper;
import com.projects.api.core.order.Order;
import com.projects.microservices.core.order.persistence.OrderEntity;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order entityToApi(OrderEntity entity);
    OrderEntity apiToEntity(Order order);
}
