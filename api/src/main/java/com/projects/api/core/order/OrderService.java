package com.projects.api.core.order;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {

    @PostMapping("/v1/orders")
    Mono<Order> createOrder(Order order);

    @GetMapping("/v1/orders/{orderId}")
    Mono<Order> getOrder(String orderId);

    @GetMapping("/v1/orders/user/{userId}")
    Flux<Order> getOrdersByUserId(String userId);

    @PutMapping("/v1/orders/{orderId}")
    Mono<Order> updateOrder(String orderId, Order order);

    @DeleteMapping("/v1/orders/{orderId}")
    Mono<Void> cancelOrder(String orderId);

    @PatchMapping("/v1/orders/{orderId}/status")
    Mono<Order> updateOrderStatus(String orderId, OrderStatus status);

    @GetMapping("/v1/orders/{orderId}/events")
    Flux<OrderEvent> getOrderEvents(String orderId);

    @PatchMapping("/v1/orders/{orderId}/payment")
    Mono<Order> updatePaymentStatus(String orderId, String paymentStatus);

    @PatchMapping("/v1/orders/{orderId}/shipping")
    Mono<Order> updateShippingInfo(String orderId, String trackingNumber, String carrier);
}
