package se.magnus.microservices.core.order.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.order.OrderItem;
import se.magnus.microservices.core.order.persistence.OrderItemEntity;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    OrderItem entityToApi(OrderItemEntity entity);
    OrderItemEntity apiToEntity(OrderItem item);
}
