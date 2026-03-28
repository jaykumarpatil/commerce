package com.projects.microservices.core.shipping.service;

import org.mapstruct.Mapper;
import com.projects.api.core.shipping.Shipment;
import com.projects.microservices.core.shipping.persistence.ShipmentEntity;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {
    Shipment entityToApi(ShipmentEntity entity);
    ShipmentEntity apiToEntity(Shipment shipment);
}
