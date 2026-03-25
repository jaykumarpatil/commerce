package se.magnus.microservices.core.order.service;

import static java.util.logging.Level.FINE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.order.*;
import se.magnus.api.exceptions.BadRequestException;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.microservices.core.order.persistence.*;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           OrderMapper orderMapper,
                           OrderItemMapper orderItemMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    public Mono<Order> createOrder(Order order) {
        if (order.getUserId() == null || order.getUserId().isEmpty()) {
            return Mono.error(new InvalidInputException("User ID is required"));
        }
        if (order.getCartId() == null || order.getCartId().isEmpty()) {
            return Mono.error(new InvalidInputException("Cart ID is required"));
        }

        OrderEntity entity = orderMapper.apiToEntity(order);
        
        return orderRepository.save(entity)
                .log(LOG.getName(), FINE)
                .onErrorMap(DataIntegrityViolationException.class, 
                        ex -> new InvalidInputException("Duplicate order ID: " + order.getOrderId()))
                .map(orderMapper::entityToApi);
    }

    @Override
    public Mono<Order> getOrder(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .map(orderMapper::entityToApi);
    }

    @Override
    public Flux<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId)
                .map(orderMapper::entityToApi);
    }

    @Override
    public Mono<Order> updateOrder(String orderId, Order order) {
        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .flatMap(entity -> {
                    if (order.getShippingAddress() != null) entity.setShippingAddress(order.getShippingAddress());
                    if (order.getBillingAddress() != null) entity.setBillingAddress(order.getBillingAddress());
                    if (order.getPaymentMethod() != null) entity.setPaymentMethod(order.getPaymentMethod());

                    return orderRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(orderMapper::entityToApi);
                });
    }

    @Override
    public Mono<Void> cancelOrder(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .flatMap(entity -> {
                    if (!"PENDING".equals(entity.getStatus())) {
                        return Mono.error(new BadRequestException("Order cannot be cancelled. Current status: " + entity.getStatus()));
                    }
                    entity.setStatus("CANCELLED");
                    return orderRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .then(Mono.empty());
                });
    }

    @Override
    public Mono<Order> updateOrderStatus(String orderId, String status) {
        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .flatMap(entity -> {
                    // Validate status transitions
                    String currentStatus = entity.getStatus();
                    if (!isValidStatusTransition(currentStatus, status)) {
                        return Mono.error(new BadRequestException("Invalid status transition from " + currentStatus + " to " + status));
                    }
                    
                    entity.setStatus(status);
                    switch (status) {
                        case "CONFIRMED":
                            entity.setConfirmedDate(java.time.LocalDateTime.now());
                            break;
                        case "SHIPPED":
                            entity.setShippedDate(java.time.LocalDateTime.now());
                            break;
                        case "DELIVERED":
                            entity.setDeliveredDate(java.time.LocalDateTime.now());
                            break;
                    }
                    
                    return orderRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(orderMapper::entityToApi);
                });
    }

    @Override
    public Mono<Order> updatePaymentStatus(String orderId, String paymentStatus) {
        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .flatMap(entity -> {
                    entity.setPaymentStatus(paymentStatus);
                    return orderRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(orderMapper::entityToApi);
                });
    }

    @Override
    public Mono<Order> updateShippingInfo(String orderId, String trackingNumber, String carrier) {
        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .flatMap(entity -> {
                    entity.setTrackingNumber(trackingNumber);
                    entity.setCarrier(carrier);
                    return orderRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(orderMapper::entityToApi);
                });
    }

    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        switch (currentStatus) {
            case "PENDING":
                return "CONFIRMED".equals(newStatus) || "CANCELLED".equals(newStatus);
            case "CONFIRMED":
                return "SHIPPED".equals(newStatus) || "CANCELLED".equals(newStatus);
            case "SHIPPED":
                return "DELIVERED".equals(newStatus);
            case "DELIVERED":
                return false; // Cannot change status after delivery
            case "CANCELLED":
                return false; // Cannot change cancelled orders
            default:
                return false;
        }
    }
}
