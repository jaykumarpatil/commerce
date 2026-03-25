package se.magnus.microservices.core.order.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.order.Order;
import se.magnus.microservices.core.order.persistence.OrderEntity;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order entityToApi(OrderEntity entity);
    OrderEntity apiToEntity(Order order);
}
