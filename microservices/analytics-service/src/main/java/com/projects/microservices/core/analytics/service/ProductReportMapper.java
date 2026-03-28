package com.projects.microservices.core.analytics.service;

import org.mapstruct.Mapper;
import com.projects.api.core.analytics.ProductReport;
import com.projects.microservices.core.analytics.persistence.ProductReportEntity;

@Mapper(componentModel = "spring")
public interface ProductReportMapper {
    ProductReport entityToApi(ProductReportEntity entity);
    ProductReportEntity apiToEntity(ProductReport report);
}
