package com.projects.api.core.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    private String inventoryId;
    private String productId;
    private String productName;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer totalQuantity;
    private String warehouseId;
    private String location;
    private boolean inStock;
    private String status; // AVAILABLE, RESERVED, OUT_OF_STOCK
    private String createdAt;
    private String updatedAt;
}
