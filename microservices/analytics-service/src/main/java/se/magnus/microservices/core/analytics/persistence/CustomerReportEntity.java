package se.magnus.microservices.core.analytics.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("customer_reports")
public class CustomerReportEntity {
    @Id
    private Long id;
    private String reportId;
    private java.time.LocalDateTime startDate;
    private java.time.LocalDateTime endDate;
    private Integer totalCustomers;
    private Integer newCustomers;
    private Integer returningCustomers;
    private Double averageCustomerValue;
}
