package com.projects.microservices.core.product.services;

import com.projects.api.core.product.Product;
import com.projects.microservices.core.product.persistence.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

  public Product entityToApi(ProductEntity entity) {
    if (entity == null) {
      return null;
    }
    return new Product(entity.getProductId(), entity.getName(), entity.getWeight(), null);
  }

  public ProductEntity apiToEntity(Product api) {
    if (api == null) {
      return null;
    }
    return new ProductEntity(api.getProductId(), api.getName(), api.getWeight());
  }
}
