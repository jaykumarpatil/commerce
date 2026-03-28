package com.projects.microservices.order.application.port.inbound;

import com.projects.api.core.order.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderQueryPort {
    Mono<Order> getOrder(String orderId);
    Flux<Order> getOrdersByUserId(String userId);
}
