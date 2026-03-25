package se.magnus.microservices.core.payment.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

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
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, WALLET, BANK_TRANSFER
    private String paymentStatus; // PENDING, COMPLETED, FAILED, REFUNDED
    private String transactionId;
    private String cardLastFour;
    private String cardBrand;
    private java.time.LocalDateTime paymentDate;
    private String failureReason;
    private Boolean is3DSecure;
    private String createdAt;
}
