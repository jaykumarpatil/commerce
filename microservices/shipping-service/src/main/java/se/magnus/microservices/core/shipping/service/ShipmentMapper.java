package se.magnus.microservices.core.shipping.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.shipping.Shipment;
import se.magnus.microservices.core.shipping.persistence.ShipmentEntity;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {
    Shipment entityToApi(ShipmentEntity entity);
    ShipmentEntity apiToEntity(Shipment shipment);
}
