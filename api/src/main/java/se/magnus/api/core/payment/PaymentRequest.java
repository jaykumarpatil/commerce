package se.magnus.api.core.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String orderId;
    private Double amount;
    private String currency;
    private String paymentMethod;
    private String cardToken; // For Stripe tokenization
    private Boolean is3DSecure;
    private String billingAddress;
}
