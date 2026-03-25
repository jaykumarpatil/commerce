package se.magnus.microservices.core.inventory.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("inventory")
public class InventoryEntity {
    @Id
    private Long id;
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
