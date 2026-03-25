package se.magnus.microservices.core.inventory.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.inventory.StockReservation;
import se.magnus.microservices.core.inventory.persistence.StockReservationEntity;

@Mapper(componentModel = "spring")
public interface StockReservationMapper {
    StockReservation entityToApi(StockReservationEntity entity);
    StockReservationEntity apiToEntity(StockReservation item);
}
