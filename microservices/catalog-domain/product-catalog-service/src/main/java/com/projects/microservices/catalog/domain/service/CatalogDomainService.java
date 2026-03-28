package com.projects.microservices.catalog.domain.service;

import com.projects.microservices.catalog.domain.model.ProductEntity;
import org.springframework.stereotype.Service;

@Service
public class CatalogDomainService {
    
    public boolean isInStock(ProductEntity product) {
        return product.getStockQuantity() != null && product.getStockQuantity() > 0;
    }
    
    public boolean canReduceStock(ProductEntity product, int quantity) {
        return product.getStockQuantity() != null && product.getStockQuantity() >= quantity;
    }
    
    public ProductEntity reduceStock(ProductEntity product, int quantity) {
        if (canReduceStock(product, quantity)) {
            product.setStockQuantity(product.getStockQuantity() - quantity);
        }
        return product;
    }
}
