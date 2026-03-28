package com.projects.microservices.core.analytics.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CustomerReportRepository extends ReactiveCrudRepository<CustomerReportEntity, Long> {
    Mono<CustomerReportEntity> findByReportId(String reportId);
}
