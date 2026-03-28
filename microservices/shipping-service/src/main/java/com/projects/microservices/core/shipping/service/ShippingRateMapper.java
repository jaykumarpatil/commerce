package com.projects.microservices.core.shipping.service;

import org.mapstruct.Mapper;
import com.projects.api.core.shipping.ShippingRate;
import com.projects.microservices.core.shipping.persistence.ShippingRateEntity;

@Mapper(componentModel = "spring")
public interface ShippingRateMapper {
    ShippingRate entityToApi(ShippingRateEntity entity);
    ShippingRateEntity apiToEntity(ShippingRate rate);
}
