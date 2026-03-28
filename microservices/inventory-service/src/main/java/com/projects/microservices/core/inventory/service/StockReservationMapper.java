package com.projects.microservices.core.inventory.service;

import org.mapstruct.Mapper;
import com.projects.api.core.inventory.StockReservation;
import com.projects.microservices.core.inventory.persistence.StockReservationEntity;

@Mapper(componentModel = "spring")
public interface StockReservationMapper {
    StockReservation entityToApi(StockReservationEntity entity);
    StockReservationEntity apiToEntity(StockReservation item);
}
