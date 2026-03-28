package com.projects.microservices.core.cart.service;

import org.mapstruct.Mapper;
import com.projects.api.core.cart.Cart;
import com.projects.microservices.core.cart.persistence.CartEntity;

@Mapper(componentModel = "spring")
public interface CartMapper {
    Cart entityToApi(CartEntity entity);
    CartEntity apiToEntity(Cart cart);
}
