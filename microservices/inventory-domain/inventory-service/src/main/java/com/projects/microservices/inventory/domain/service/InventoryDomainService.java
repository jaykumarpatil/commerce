package com.projects.microservices.inventory.domain.service;

import com.projects.microservices.inventory.domain.model.InventoryEntity;
import org.springframework.stereotype.Service;

@Service
public class InventoryDomainService {
    
    public boolean canReserve(InventoryEntity inventory, int quantity) {
        return inventory.getAvailableQuantity() != null && 
               inventory.getAvailableQuantity() >= quantity;
    }
    
    public InventoryEntity reserve(InventoryEntity inventory, int quantity) {
        if (canReserve(inventory, quantity)) {
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
            inventory.setReservedQuantity(
                (inventory.getReservedQuantity() != null ? inventory.getReservedQuantity() : 0) + quantity
            );
        }
        return inventory;
    }
    
    public InventoryEntity release(InventoryEntity inventory, int quantity) {
        if (inventory.getReservedQuantity() != null && inventory.getReservedQuantity() >= quantity) {
            inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
            inventory.setAvailableQuantity(
                (inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0) + quantity
            );
        }
        return inventory;
    }
}
