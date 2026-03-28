package se.magnus.microservices.order.application.port.inbound;

import se.magnus.api.core.order.Order;
import reactor.core.publisher.Mono;

public interface OrderCommandPort {
    Mono<Order> createOrder(Order order);
    Mono<Order> updateOrder(String orderId, Order order);
    Mono<Void> cancelOrder(String orderId);
    Mono<Order> updateOrderStatus(String orderId, String status);
    Mono<Order> updatePaymentStatus(String orderId, String paymentStatus);
    Mono<Order> updateShippingInfo(String orderId, String trackingNumber, String carrier);
}
