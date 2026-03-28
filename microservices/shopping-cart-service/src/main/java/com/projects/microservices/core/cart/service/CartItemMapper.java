package com.projects.microservices.core.cart.service;

import org.mapstruct.Mapper;
import com.projects.api.core.cart.CartItem;
import com.projects.microservices.core.cart.persistence.CartItemEntity;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    CartItem entityToApi(CartItemEntity entity);
    CartItemEntity apiToEntity(CartItem item);
}
