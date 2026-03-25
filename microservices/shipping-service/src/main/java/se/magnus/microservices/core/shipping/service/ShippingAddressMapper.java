package se.magnus.microservices.core.shipping.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.shipping.ShippingAddress;
import se.magnus.microservices.core.shipping.persistence.ShippingAddressEntity;

@Mapper(componentModel = "spring")
public interface ShippingAddressMapper {
    ShippingAddress entityToApi(ShippingAddressEntity entity);
    ShippingAddressEntity apiToEntity(ShippingAddress address);
}
