package se.magnus.api.core.analytics;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AnalyticsService {

    @GetMapping("/v1/analytics/sales")
    Mono<SalesReport> getSalesReport(@RequestParam java.time.LocalDateTime startDate,
                                      @RequestParam java.time.LocalDateTime endDate);

    @GetMapping("/v1/analytics/customers")
    Mono<CustomerReport> getCustomerReport(@RequestParam java.time.LocalDateTime startDate,
                                           @RequestParam java.time.LocalDateTime endDate);

    @GetMapping("/v1/analytics/products")
    Mono<ProductReport> getProductReport(@RequestParam java.time.LocalDateTime startDate,
                                         @RequestParam java.time.LocalDateTime endDate);

    @GetMapping("/v1/analytics/export/sales")
    Mono<byte[]> exportSalesReport(@RequestParam java.time.LocalDateTime startDate,
                                   @RequestParam java.time.LocalDateTime endDate);

    @GetMapping("/v1/analytics/export/customers")
    Mono<byte[]> exportCustomerReport(@RequestParam java.time.LocalDateTime startDate,
                                      @RequestParam java.time.LocalDateTime endDate);

    @GetMapping("/v1/analytics/dashboard")
    Mono<DashboardData> getDashboardData();
}
