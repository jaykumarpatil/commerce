package se.magnus.microservices.core.analytics.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.analytics.SalesReport;
import se.magnus.microservices.core.analytics.persistence.SalesReportEntity;

@Mapper(componentModel = "spring")
public interface SalesReportMapper {
    SalesReport entityToApi(SalesReportEntity entity);
    SalesReportEntity apiToEntity(SalesReport report);
}
