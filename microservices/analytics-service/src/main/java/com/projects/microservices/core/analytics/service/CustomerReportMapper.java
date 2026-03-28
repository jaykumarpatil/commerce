package com.projects.microservices.core.analytics.service;

import org.mapstruct.Mapper;
import com.projects.api.core.analytics.CustomerReport;
import com.projects.microservices.core.analytics.persistence.CustomerReportEntity;

@Mapper(componentModel = "spring")
public interface CustomerReportMapper {
    CustomerReport entityToApi(CustomerReportEntity entity);
    CustomerReportEntity apiToEntity(CustomerReport report);
}
