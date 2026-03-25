package se.magnus.microservices.core.shipping.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.shipping.ShippingRate;
import se.magnus.microservices.core.shipping.persistence.ShippingRateEntity;

@Mapper(componentModel = "spring")
public interface ShippingRateMapper {
    ShippingRate entityToApi(ShippingRateEntity entity);
    ShippingRateEntity apiToEntity(ShippingRate rate);
}
