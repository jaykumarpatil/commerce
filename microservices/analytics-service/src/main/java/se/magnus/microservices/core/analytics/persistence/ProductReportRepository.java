package se.magnus.microservices.core.analytics.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ProductReportRepository extends ReactiveCrudRepository<ProductReportEntity, Long> {
    Mono<ProductReportEntity> findByReportId(String reportId);
}
