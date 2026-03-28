package com.projects.microservices.core.cart.service;

import org.mapstruct.Mapper;
import com.projects.api.core.cart.CartItemOption;
import com.projects.microservices.core.cart.persistence.CartItemOptionEntity;

@Mapper(componentModel = "spring")
public interface CartItemOptionMapper {
    CartItemOption entityToApi(CartItemOptionEntity entity);
    CartItemOptionEntity apiToEntity(CartItemOption item);
}
