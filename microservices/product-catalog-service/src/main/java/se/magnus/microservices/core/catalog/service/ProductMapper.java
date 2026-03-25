package se.magnus.microservices.core.catalog.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.catalog.Product;
import se.magnus.microservices.core.catalog.persistence.ProductEntity;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product entityToApi(ProductEntity entity);
    ProductEntity apiToEntity(Product product);
}
