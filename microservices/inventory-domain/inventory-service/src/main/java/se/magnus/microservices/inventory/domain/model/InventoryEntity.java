package se.magnus.microservices.inventory.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "inventory")
public class InventoryEntity {
    @Id
    private String id;
    private String productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer warehouseId;
}
