package com.projects.microservices.core.analytics.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import com.projects.api.core.analytics.*;
import com.projects.util.http.ServiceUtil;

@RestController
public class AnalyticsController implements AnalyticsService {

    private final AnalyticsService analyticsService;
    private final ServiceUtil serviceUtil;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService, ServiceUtil serviceUtil) {
        this.analyticsService = analyticsService;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<SalesReport> getSalesReport(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return analyticsService.getSalesReport(startDate, endDate);
    }

    @Override
    public Mono<CustomerReport> getCustomerReport(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return analyticsService.getCustomerReport(startDate, endDate);
    }

    @Override
    public Mono<ProductReport> getProductReport(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return analyticsService.getProductReport(startDate, endDate);
    }

    @Override
    public Mono<byte[]> exportSalesReport(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return analyticsService.exportSalesReport(startDate, endDate);
    }

    @Override
    public Mono<byte[]> exportCustomerReport(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return analyticsService.exportCustomerReport(startDate, endDate);
    }

    @Override
    public Mono<DashboardData> getDashboardData() {
        return analyticsService.getDashboardData();
    }
}
