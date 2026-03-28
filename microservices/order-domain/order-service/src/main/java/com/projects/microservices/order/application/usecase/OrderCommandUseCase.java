package com.projects.microservices.order.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.projects.api.core.order.Order;
import com.projects.api.core.order.OrderItem;
import com.projects.api.exceptions.BadRequestException;
import com.projects.api.exceptions.InvalidInputException;
import com.projects.api.exceptions.NotFoundException;
import com.projects.microservices.order.application.port.inbound.OrderCommandPort;
import com.projects.microservices.order.application.port.outbound.OrderRepositoryPort;
import com.projects.microservices.order.application.port.outbound.OrderEventPublisherPort;
import com.projects.microservices.order.domain.event.OrderEvent;
import com.projects.microservices.order.domain.model.OrderEntity;
import com.projects.microservices.order.domain.model.OrderItemEntity;
import com.projects.microservices.order.domain.service.OrderDomainService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderCommandUseCase implements OrderCommandPort {

    private static final Logger LOG = LoggerFactory.getLogger(OrderCommandUseCase.class);

    private final OrderRepositoryPort repository;
    private final OrderDomainService domainService;
    private final OrderEventPublisherPort eventPublisher;

    public OrderCommandUseCase(OrderRepositoryPort repository, OrderDomainService domainService, OrderEventPublisherPort eventPublisher) {
        this.repository = repository;
        this.domainService = domainService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Mono<Order> createOrder(Order order) {
        if (order.getUserId() == null || order.getUserId().isBlank()) {
            return Mono.error(new InvalidInputException("User ID is required"));
        }
        if (order.getCartId() == null || order.getCartId().isBlank()) {
            return Mono.error(new InvalidInputException("Cart ID is required"));
        }

        OrderEntity entity = mapToEntity(order);
        entity.setOrderId(UUID.randomUUID().toString());
        entity.setStatus("PENDING");
        entity.setPaymentStatus("PENDING");
        entity.setOrderDate(LocalDateTime.now());
        entity.setCreatedAt(LocalDateTime.now().toString());

        return repository.save(entity)
                .doOnSuccess(saved -> {
                    OrderEvent.Created event = new OrderEvent.Created(
                            saved.getOrderId(),
                            saved.getUserId(),
                            saved.getGrandTotal(),
                            Instant.now()
                    );
                    eventPublisher.publish(event);
                    LOG.info("Order created: {}", saved.getOrderId());
                })
                .map(this::mapToApi);
    }

    @Override
    public Mono<Order> updateOrder(String orderId, Order order) {
        return repository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .flatMap(entity -> {
                    if (order.getShippingAddress() != null) {
                        entity.setShippingAddress(order.getShippingAddress());
                    }
                    if (order.getBillingAddress() != null) {
                        entity.setBillingAddress(order.getBillingAddress());
                    }
                    if (order.getPaymentMethod() != null) {
                        entity.setPaymentMethod(order.getPaymentMethod());
                    }
                    entity.setUpdatedAt(LocalDateTime.now().toString());
                    return repository.save(entity).map(this::mapToApi);
                });
    }

    @Override
    public Mono<Void> cancelOrder(String orderId) {
        return repository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .flatMap(entity -> {
                    if (!domainService.canCancel(entity)) {
                        return Mono.error(new BadRequestException("Order cannot be cancelled. Status: " + entity.getStatus()));
                    }
                    String previousStatus = entity.getStatus();
                    entity.setStatus("CANCELLED");
                    entity.setUpdatedAt(LocalDateTime.now().toString());
                    
                    return repository.save(entity)
                            .doOnSuccess(saved -> {
                                eventPublisher.publish(new OrderEvent.Cancelled(orderId, "User cancelled", Instant.now()));
                                LOG.info("Order {} cancelled (was {})", orderId, previousStatus);
                            });
                })
                .then();
    }

    @Override
    public Mono<Order> updateOrderStatus(String orderId, String status) {
        return repository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .flatMap(entity -> {
                    String previousStatus = entity.getStatus();
                    
                    if (!domainService.canTransitionStatus(entity, status)) {
                        return Mono.error(new BadRequestException("Invalid status transition from " + previousStatus + " to " + status));
                    }
                    
                    domainService.applyStatusChange(entity, status);
                    
                    return repository.save(entity)
                            .doOnSuccess(saved -> {
                                eventPublisher.publish(new OrderEvent.StatusChanged(orderId, previousStatus, status, Instant.now()));
                                LOG.info("Order {} status: {} -> {}", orderId, previousStatus, status);
                            })
                            .map(this::mapToApi);
                });
    }

    @Override
    public Mono<Order> updatePaymentStatus(String orderId, String paymentStatus) {
        return repository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .flatMap(entity -> {
                    entity.setPaymentStatus(paymentStatus);
                    entity.setUpdatedAt(LocalDateTime.now().toString());
                    
                    if ("COMPLETED".equals(paymentStatus)) {
                        eventPublisher.publish(new OrderEvent.PaymentReceived(
                                orderId, UUID.randomUUID().toString(), entity.getGrandTotal(), Instant.now()));
                    }
                    
                    return repository.save(entity).map(this::mapToApi);
                });
    }

    @Override
    public Mono<Order> updateShippingInfo(String orderId, String trackingNumber, String carrier) {
        return repository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .flatMap(entity -> {
                    entity.setTrackingNumber(trackingNumber);
                    entity.setCarrier(carrier);
                    entity.setUpdatedAt(LocalDateTime.now().toString());
                    
                    if (trackingNumber != null) {
                        eventPublisher.publish(new OrderEvent.Shipped(orderId, trackingNumber, carrier, Instant.now()));
                    }
                    
                    return repository.save(entity).map(this::mapToApi);
                });
    }

    private OrderEntity mapToEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderId(order.getOrderId());
        entity.setUserId(order.getUserId());
        entity.setCartId(order.getCartId());
        entity.setShippingAddress(order.getShippingAddress());
        entity.setBillingAddress(order.getBillingAddress());
        entity.setPaymentMethod(order.getPaymentMethod());
        
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                OrderItemEntity itemEntity = new OrderItemEntity();
                itemEntity.setOrderItemId(item.getOrderItemId());
                itemEntity.setProductId(item.getProductId());
                itemEntity.setProductName(item.getProductName());
                itemEntity.setProductImage(item.getProductImage());
                itemEntity.setUnitPrice(item.getUnitPrice());
                itemEntity.setQuantity(item.getQuantity());
                itemEntity.setDiscountAmount(item.getDiscountAmount());
                itemEntity.setTotalPrice(item.getTotalPrice());
                entity.addItem(itemEntity);
            }
        }
        
        return entity;
    }

    private Order mapToApi(OrderEntity entity) {
        if (entity == null) return null;
        
        Order order = new Order();
        order.setOrderId(entity.getOrderId());
        order.setUserId(entity.getUserId());
        order.setCartId(entity.getCartId());
        order.setSubtotal(entity.getSubtotal());
        order.setDiscountTotal(entity.getDiscountTotal());
        order.setTaxAmount(entity.getTaxAmount());
        order.setShippingCost(entity.getShippingCost());
        order.setGrandTotal(entity.getGrandTotal());
        order.setStatus(entity.getStatus());
        order.setShippingAddress(entity.getShippingAddress());
        order.setBillingAddress(entity.getBillingAddress());
        order.setPaymentMethod(entity.getPaymentMethod());
        order.setPaymentStatus(entity.getPaymentStatus());
        order.setTrackingNumber(entity.getTrackingNumber());
        order.setCarrier(entity.getCarrier());
        order.setOrderDate(entity.getOrderDate());
        order.setConfirmedDate(entity.getConfirmedDate());
        order.setShippedDate(entity.getShippedDate());
        order.setDeliveredDate(entity.getDeliveredDate());
        
        if (entity.getItems() != null) {
            order.setItems(entity.getItems().stream().map(this::mapItemToApi).toList());
        }
        
        return order;
    }

    private OrderItem mapItemToApi(OrderItemEntity entity) {
        OrderItem item = new OrderItem();
        item.setOrderItemId(entity.getOrderItemId());
        item.setProductId(entity.getProductId());
        item.setProductName(entity.getProductName());
        item.setProductImage(entity.getProductImage());
        item.setUnitPrice(entity.getUnitPrice());
        item.setQuantity(entity.getQuantity());
        item.setDiscountAmount(entity.getDiscountAmount());
        item.setTotalPrice(entity.getTotalPrice());
        item.setStatus(entity.getStatus());
        return item;
    }
}
