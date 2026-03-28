package se.magnus.microservices.payment.domain.service;

import se.magnus.microservices.payment.domain.model.PaymentEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentDomainService {

    public PaymentEntity createPayment(PaymentEntity payment, String orderId, Double amount, String currency) {
        payment.setPaymentId(java.util.UUID.randomUUID().toString());
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setPaymentStatus("PENDING");
        payment.setPaymentDate(LocalDateTime.now());
        payment.setCreatedAt(LocalDateTime.now().toString());
        return payment;
    }

    public boolean canProcessPayment(PaymentEntity payment) {
        return payment.getAmount() != null && payment.getAmount() > 0;
    }

    public PaymentEntity markAsCompleted(PaymentEntity payment, String transactionId) {
        payment.setPaymentStatus("COMPLETED");
        payment.setTransactionId(transactionId);
        return payment;
    }

    public PaymentEntity markAsFailed(PaymentEntity payment, String reason) {
        payment.setPaymentStatus("FAILED");
        payment.setFailureReason(reason);
        return payment;
    }

    public boolean canRefund(PaymentEntity payment) {
        return "COMPLETED".equals(payment.getPaymentStatus());
    }

    public PaymentEntity markAsRefunded(PaymentEntity payment) {
        payment.setPaymentStatus("REFUNDED");
        return payment;
    }
}
