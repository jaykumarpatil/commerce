package com.projects.microservices.core.shipping.service;

import org.mapstruct.Mapper;
import com.projects.api.core.shipping.ShippingAddress;
import com.projects.microservices.core.shipping.persistence.ShippingAddressEntity;

@Mapper(componentModel = "spring")
public interface ShippingAddressMapper {
    ShippingAddress entityToApi(ShippingAddressEntity entity);
    ShippingAddressEntity apiToEntity(ShippingAddress address);
}
