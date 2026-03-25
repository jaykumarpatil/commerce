package se.magnus.microservices.core.catalog.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.catalog.Variant;
import se.magnus.microservices.core.catalog.persistence.VariantEntity;

@Mapper(componentModel = "spring")
public interface VariantMapper {
    Variant entityToApi(VariantEntity entity);
    VariantEntity apiToEntity(Variant variant);
}
