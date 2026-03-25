package se.magnus.microservices.core.cart.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.cart.CartItem;
import se.magnus.microservices.core.cart.persistence.CartItemEntity;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    CartItem entityToApi(CartItemEntity entity);
    CartItemEntity apiToEntity(CartItem item);
}
