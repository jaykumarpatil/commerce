package se.magnus.microservices.core.cart.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.cart.Cart;
import se.magnus.microservices.core.cart.persistence.CartEntity;

@Mapper(componentModel = "spring")
public interface CartMapper {
    Cart entityToApi(CartEntity entity);
    CartEntity apiToEntity(Cart cart);
}
