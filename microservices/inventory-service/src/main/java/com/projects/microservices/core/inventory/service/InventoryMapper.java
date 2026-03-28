package com.projects.microservices.core.inventory.service;

import org.mapstruct.Mapper;
import com.projects.api.core.inventory.InventoryItem;
import com.projects.microservices.core.inventory.persistence.InventoryEntity;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    InventoryItem entityToApi(InventoryEntity entity);
    InventoryEntity apiToEntity(InventoryItem item);
}
