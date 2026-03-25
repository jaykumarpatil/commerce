package se.magnus.microservices.core.analytics.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.analytics.CustomerReport;
import se.magnus.microservices.core.analytics.persistence.CustomerReportEntity;

@Mapper(componentModel = "spring")
public interface CustomerReportMapper {
    CustomerReport entityToApi(CustomerReportEntity entity);
    CustomerReportEntity apiToEntity(CustomerReport report);
}
