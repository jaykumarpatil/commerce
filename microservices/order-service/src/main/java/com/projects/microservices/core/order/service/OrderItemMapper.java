package com.projects.microservices.core.order.service;

import org.mapstruct.Mapper;
import com.projects.api.core.order.OrderItem;
import com.projects.microservices.core.order.persistence.OrderItemEntity;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    OrderItem entityToApi(OrderItemEntity entity);
    OrderItemEntity apiToEntity(OrderItem item);
}
