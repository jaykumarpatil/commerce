package se.magnus.microservices.core.analytics.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SalesReportRepository extends ReactiveCrudRepository<SalesReportEntity, Long> {
    Mono<SalesReportEntity> findByReportId(String reportId);
}
