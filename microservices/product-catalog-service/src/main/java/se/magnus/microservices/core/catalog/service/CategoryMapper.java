package se.magnus.microservices.core.catalog.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.catalog.Category;
import se.magnus.microservices.core.catalog.persistence.CategoryEntity;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category entityToApi(CategoryEntity entity);
    CategoryEntity apiToEntity(Category category);
}
