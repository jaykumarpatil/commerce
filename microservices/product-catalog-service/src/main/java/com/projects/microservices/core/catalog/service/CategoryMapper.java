package com.projects.microservices.core.catalog.service;

import org.mapstruct.Mapper;
import com.projects.api.core.catalog.Category;
import com.projects.microservices.core.catalog.persistence.CategoryEntity;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category entityToApi(CategoryEntity entity);
    CategoryEntity apiToEntity(Category category);
}
