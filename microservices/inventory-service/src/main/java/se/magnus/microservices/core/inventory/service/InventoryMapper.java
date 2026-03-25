package se.magnus.microservices.core.inventory.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.inventory.InventoryItem;
import se.magnus.microservices.core.inventory.persistence.InventoryEntity;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    InventoryItem entityToApi(InventoryEntity entity);
    InventoryEntity apiToEntity(InventoryItem item);
}
