package se.magnus.microservices.core.analytics.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.analytics.ProductReport;
import se.magnus.microservices.core.analytics.persistence.ProductReportEntity;

@Mapper(componentModel = "spring")
public interface ProductReportMapper {
    ProductReport entityToApi(ProductReportEntity entity);
    ProductReportEntity apiToEntity(ProductReport report);
}
