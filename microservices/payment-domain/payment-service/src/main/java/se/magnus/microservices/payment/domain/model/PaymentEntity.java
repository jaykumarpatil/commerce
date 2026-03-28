package se.magnus.microservices.payment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("payments")
public class PaymentEntity {
    @Id
    private Long id;
    private String paymentId;
    private String orderId;
    private Double amount;
    private String currency;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionId;
    private String cardLastFour;
    private String cardBrand;
    private LocalDateTime paymentDate;
    private String failureReason;
    private Boolean is3DSecure;
    private String createdAt;
    
    public boolean canRefund() {
        return "COMPLETED".equals(paymentStatus);
    }
    
    public boolean isFailed() {
        return "FAILED".equals(paymentStatus);
    }
    
    public boolean isCompleted() {
        return "COMPLETED".equals(paymentStatus);
    }
}
