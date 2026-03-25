package se.magnus.microservices.core.cart.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.cart.CartItemOption;
import se.magnus.microservices.core.cart.persistence.CartItemOptionEntity;

@Mapper(componentModel = "spring")
public interface CartItemOptionMapper {
    CartItemOption entityToApi(CartItemOptionEntity entity);
    CartItemOptionEntity apiToEntity(CartItemOption item);
}
