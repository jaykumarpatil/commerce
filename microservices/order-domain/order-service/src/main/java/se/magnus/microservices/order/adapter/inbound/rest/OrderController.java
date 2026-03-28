package se.magnus.microservices.order.adapter.inbound.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.order.Order;
import se.magnus.microservices.order.application.port.inbound.OrderCommandPort;
import se.magnus.microservices.order.application.port.inbound.OrderQueryPort;

@RestController
public class OrderController {

    private final OrderCommandPort commandPort;
    private final OrderQueryPort queryPort;

    @Autowired
    public OrderController(OrderCommandPort commandPort, OrderQueryPort queryPort) {
        this.commandPort = commandPort;
        this.queryPort = queryPort;
    }

    public Mono<Order> createOrder(Order order) {
        return commandPort.createOrder(order);
    }

    public Mono<Order> getOrder(String orderId) {
        return queryPort.getOrder(orderId);
    }

    public Flux<Order> getOrdersByUserId(String userId) {
        return queryPort.getOrdersByUserId(userId);
    }

    public Mono<Order> updateOrder(String orderId, Order order) {
        return commandPort.updateOrder(orderId, order);
    }

    public Mono<Void> cancelOrder(String orderId) {
        return commandPort.cancelOrder(orderId);
    }

    public Mono<Order> updateOrderStatus(String orderId, String status) {
        return commandPort.updateOrderStatus(orderId, status);
    }

    public Mono<Order> updatePaymentStatus(String orderId, String paymentStatus) {
        return commandPort.updatePaymentStatus(orderId, paymentStatus);
    }

    public Mono<Order> updateShippingInfo(String orderId, String trackingNumber, String carrier) {
        return commandPort.updateShippingInfo(orderId, trackingNumber, carrier);
    }
}
