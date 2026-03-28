package se.magnus.microservices.payment.adapter.outbound.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import se.magnus.microservices.payment.application.port.outbound.PaymentGatewayPort;
import se.magnus.microservices.payment.application.port.outbound.PaymentRequest;
import se.magnus.microservices.payment.application.port.outbound.PaymentResult;
import se.magnus.microservices.payment.application.port.outbound.RefundResult;

import java.util.UUID;

@Service
public class MockPaymentGatewayAdapter implements PaymentGatewayPort {

    private static final Logger LOG = LoggerFactory.getLogger(MockPaymentGatewayAdapter.class);

    @Value("${payment.gateway.mock:true}")
    private boolean useMock;

    @Override
    public Mono<PaymentResult> charge(PaymentRequest request) {
        LOG.info("Processing payment for order {} amount {}", request.orderId(), request.amount());
        
        if (useMock) {
            return Mono.just(new PaymentResult(
                    "mock_" + UUID.randomUUID().toString().substring(0, 8),
                    true,
                    null
            ));
        }
        
        return Mono.just(new PaymentResult(
                "stripe_" + UUID.randomUUID().toString().substring(0, 8),
                true,
                null
        ));
    }

    @Override
    public Mono<RefundResult> refund(String transactionId, Double amount) {
        LOG.info("Processing refund for transaction {} amount {}", transactionId, amount);
        
        if (useMock) {
            return Mono.just(new RefundResult(
                    "mock_refund_" + UUID.randomUUID().toString().substring(0, 8),
                    true,
                    null
            ));
        }
        
        return Mono.just(new RefundResult(
                "stripe_refund_" + UUID.randomUUID().toString().substring(0, 8),
                true,
                null
        ));
    }
}
