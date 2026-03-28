package com.projects.microservices.core.catalog.service;

import org.mapstruct.Mapper;
import com.projects.api.core.catalog.Variant;
import com.projects.microservices.core.catalog.persistence.VariantEntity;

@Mapper(componentModel = "spring")
public interface VariantMapper {
    Variant entityToApi(VariantEntity entity);
    VariantEntity apiToEntity(Variant variant);
}
