package com.projects.microservices.core.analytics.service;

import org.mapstruct.Mapper;
import com.projects.api.core.analytics.SalesReport;
import com.projects.microservices.core.analytics.persistence.SalesReportEntity;

@Mapper(componentModel = "spring")
public interface SalesReportMapper {
    SalesReport entityToApi(SalesReportEntity entity);
    SalesReportEntity apiToEntity(SalesReport report);
}
