package com.projects.microservices.core.catalog.service;

import org.mapstruct.Mapper;
import com.projects.api.core.catalog.Product;
import com.projects.microservices.core.catalog.persistence.ProductEntity;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product entityToApi(ProductEntity entity);
    ProductEntity apiToEntity(Product product);
}
