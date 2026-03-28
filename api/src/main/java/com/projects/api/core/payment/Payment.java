package com.projects.api.core.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
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
