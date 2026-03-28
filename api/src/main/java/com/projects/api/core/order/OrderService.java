package com.projects.api.core.order;

import com.projects.api.core.common.PaginationRequest;
import com.projects.api.core.common.PaginationResponse;
import com.projects.api.core.payment.PaymentStatus;
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

    default Mono<PaginationResponse<Order>> getOrdersByUserId(String userId, PaginationRequest paginationRequest) {
        final PaginationRequest request = paginationRequest == null ? new PaginationRequest() : paginationRequest;
        return getOrdersByUserId(userId)
            .skip((long) request.getPage() * request.getSize())
            .take(request.getSize())
            .collectList()
            .map(items -> PaginationResponse.of(items, request.getPage(), request.getSize()));
    }

    @PutMapping("/v1/orders/{orderId}")
    Mono<Order> updateOrder(String orderId, Order order);

    @DeleteMapping("/v1/orders/{orderId}")
    Mono<Void> cancelOrder(String orderId);

    Mono<Order> updateOrderStatus(String orderId, OrderStatus status);

    @Deprecated
    @PatchMapping("/v1/orders/{orderId}/status")
    Mono<Order> updateOrderStatus(String orderId, OrderStatus status);

    @GetMapping("/v1/orders/{orderId}/events")
    Flux<OrderEvent> getOrderEvents(String orderId);

    @Deprecated
    @PatchMapping("/v1/orders/{orderId}/payment")
    default Mono<Order> updatePaymentStatus(String orderId, String paymentStatus) {
        return updatePaymentStatus(orderId, PaymentStatus.from(paymentStatus));
    }

    @PatchMapping("/v1/orders/{orderId}/shipping")
    Mono<Order> updateShippingInfo(String orderId, String trackingNumber, String carrier);
}
