package com.projects.microservices.core.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.projects.api.core.order.*;
import com.projects.util.http.ServiceUtil;

@RestController
public class OrderController implements OrderService {

    private final OrderService orderService;
    private final ServiceUtil serviceUtil;

    @Autowired
    public OrderController(OrderService orderService, ServiceUtil serviceUtil) {
        this.orderService = orderService;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Order> createOrder(Order order) {
        return orderService.createOrder(order);
    }

    @Override
    public Mono<Order> getOrder(String orderId) {
        return orderService.getOrder(orderId);
    }

    @Override
    public Flux<Order> getOrdersByUserId(String userId) {
        return orderService.getOrdersByUserId(userId);
    }

    @Override
    public Mono<Order> updateOrder(String orderId, Order order) {
        return orderService.updateOrder(orderId, order);
    }

    @Override
    public Mono<Void> cancelOrder(String orderId) {
        return orderService.cancelOrder(orderId);
    }

    @Override
    public Mono<Order> updateOrderStatus(String orderId, OrderStatus status) {
        return orderService.updateOrderStatus(orderId, status);
    }

    @Override
    public Mono<Order> updatePaymentStatus(String orderId, com.projects.api.core.payment.PaymentStatus paymentStatus) {
        return orderService.updatePaymentStatus(orderId, paymentStatus);
    }

    @Override
    public Flux<OrderEvent> getOrderEvents(String orderId) {
        return orderService.getOrderEvents(orderId);
    }

    @Override
    public Mono<Order> updateShippingInfo(String orderId, String trackingNumber, String carrier) {
        return orderService.updateShippingInfo(orderId, trackingNumber, carrier);
    }
}
