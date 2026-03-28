package com.projects.microservices.core.catalog.outbound.port;

import com.projects.api.core.inventory.InventoryItem;
import reactor.core.publisher.Mono;

public interface InventoryReadPort {
    Mono<InventoryItem> getInventoryByProductId(String productId);
}
