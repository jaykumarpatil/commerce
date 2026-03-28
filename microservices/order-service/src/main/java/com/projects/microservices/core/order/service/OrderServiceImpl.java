package com.projects.microservices.core.order.service;

import com.projects.api.core.cart.Cart;
import com.projects.api.core.inventory.StockReservation;
import com.projects.api.core.notification.Notification;
import com.projects.api.core.order.Order;
import com.projects.api.core.order.OrderEvent;
import com.projects.api.core.order.OrderItem;
import com.projects.api.core.order.OrderService;
import com.projects.api.core.order.OrderStatus;
import com.projects.api.core.payment.Payment;
import com.projects.api.core.payment.PaymentRequest;
import com.projects.api.exceptions.BadRequestException;
import com.projects.api.exceptions.InvalidInputException;
import com.projects.api.exceptions.NotFoundException;
import com.projects.microservices.core.order.persistence.OrderEntity;
import com.projects.microservices.core.order.persistence.OrderEventEntity;
import com.projects.microservices.core.order.persistence.OrderEventRepository;
import com.projects.microservices.core.order.persistence.OrderItemRepository;
import com.projects.microservices.core.order.persistence.OrderRepository;
import com.projects.microservices.core.order.service.model.SagaState;
import com.projects.microservices.core.order.service.port.outbound.InventoryPort;
import com.projects.microservices.core.order.service.port.outbound.NotificationPort;
import com.projects.microservices.core.order.service.port.outbound.PaymentPort;
import com.projects.microservices.core.order.service.port.outbound.ShippingPort;
import com.projects.microservices.core.order.service.port.outbound.ShoppingCartPort;
import com.projects.microservices.core.order.service.port.outbound.UserPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.logging.Level.FINE;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderServiceImpl.class);

    private static final Map<OrderStatus, EnumSet<OrderStatus>> STATUS_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        STATUS_TRANSITIONS.put(OrderStatus.PENDING, EnumSet.of(OrderStatus.INVENTORY_RESERVED, OrderStatus.CANCELLED, OrderStatus.FAILED));
        STATUS_TRANSITIONS.put(OrderStatus.INVENTORY_RESERVED, EnumSet.of(OrderStatus.PAYMENT_AUTHORIZED, OrderStatus.CANCELLED, OrderStatus.FAILED));
        STATUS_TRANSITIONS.put(OrderStatus.PAYMENT_AUTHORIZED, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED, OrderStatus.FAILED, OrderStatus.REFUNDED));
        STATUS_TRANSITIONS.put(OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED, OrderStatus.REFUNDED));
        STATUS_TRANSITIONS.put(OrderStatus.SHIPPED, EnumSet.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED));
        STATUS_TRANSITIONS.put(OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class));
        STATUS_TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
        STATUS_TRANSITIONS.put(OrderStatus.FAILED, EnumSet.noneOf(OrderStatus.class));
        STATUS_TRANSITIONS.put(OrderStatus.REFUNDED, EnumSet.noneOf(OrderStatus.class));
    }

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderEventRepository orderEventRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderEventMapper orderEventMapper;
    private final ShoppingCartPort shoppingCartPort;
    private final InventoryPort inventoryPort;
    private final PaymentPort paymentPort;
    private final ShippingPort shippingPort;
    private final NotificationPort notificationPort;
    private final UserPort userPort;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            OrderEventRepository orderEventRepository,
                            OrderMapper orderMapper,
                            OrderItemMapper orderItemMapper,
                            OrderEventMapper orderEventMapper,
                            ShoppingCartPort shoppingCartPort,
                            InventoryPort inventoryPort,
                            PaymentPort paymentPort,
                            ShippingPort shippingPort,
                            NotificationPort notificationPort,
                            UserPort userPort) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderEventRepository = orderEventRepository;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderEventMapper = orderEventMapper;
        this.shoppingCartPort = shoppingCartPort;
        this.inventoryPort = inventoryPort;
        this.paymentPort = paymentPort;
        this.shippingPort = shippingPort;
        this.notificationPort = notificationPort;
        this.userPort = userPort;
    }

    @Override
    public Mono<Order> createOrder(Order order) {
        if (order.getUserId() == null || order.getUserId().isEmpty()) {
            return Mono.error(new InvalidInputException("User ID is required"));
        }
        if (order.getCartId() == null || order.getCartId().isEmpty()) {
            return Mono.error(new InvalidInputException("Cart ID is required"));
        }

        if (order.getOrderId() == null || order.getOrderId().isBlank()) {
            order.setOrderId(UUID.randomUUID().toString());
        }
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        SagaState sagaState = new SagaState(order.getOrderId());

        return userPort.getUserById(order.getUserId())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + order.getUserId())))
                .then(shoppingCartPort.getCart(order.getCartId()))
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found: " + order.getCartId())))
                .flatMap(cart -> validateNonEmptyCart(cart)
                        .then(populateOrderFromCart(order, cart))
                        .then(recordEvent(order.getOrderId(), "CART_VALIDATED", order.getStatus(), "Cart validated for checkout"))
                        .then(reserveInventoryAllOrNothing(order, sagaState))
                        .then(changeStatus(order, OrderStatus.INVENTORY_RESERVED, "Inventory reserved for all order items"))
                        .then(authorizePayment(order, sagaState))
                        .then(changeStatus(order, OrderStatus.PAYMENT_AUTHORIZED, "Payment authorized"))
                        .then(persistOrderAggregate(order))
                        .then(changeStatus(order, OrderStatus.CONFIRMED, "Order persisted"))
                        .then(persistOrderStatus(order))
                        .then(shoppingCartPort.clearCart(order.getCartId()))
                        .then(recordEvent(order.getOrderId(), "CART_CLEARED", order.getStatus(), "Shopping cart cleared after order placement"))
                        .then(sendConfirmation(order))
                        .then(getOrder(order.getOrderId())))
                .onErrorResume(ex -> compensateSaga(order, sagaState, ex));
    }

    @Override
    public Mono<Order> getOrder(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .map(orderMapper::entityToApi)
                .flatMap(this::attachOrderItems);
    }

    @Override
    public Flux<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId)
                .map(orderMapper::entityToApi)
                .flatMap(this::attachOrderItems);
    }

    @Override
    public Mono<Order> updateOrder(String orderId, Order order) {
        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .flatMap(entity -> {
                    if (order.getShippingAddress() != null) entity.setShippingAddress(order.getShippingAddress());
                    if (order.getBillingAddress() != null) entity.setBillingAddress(order.getBillingAddress());
                    if (order.getPaymentMethod() != null) entity.setPaymentMethod(order.getPaymentMethod());

                    entity.setUpdatedAt(LocalDateTime.now().toString());

                    return orderRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(orderMapper::entityToApi)
                            .flatMap(this::attachOrderItems);
                });
    }

    @Override
    public Mono<Void> cancelOrder(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .flatMap(entity -> {
                    OrderStatus currentStatus = OrderStatus.valueOf(entity.getStatus());
                    if (!isValidStatusTransition(currentStatus, OrderStatus.CANCELLED)) {
                        return Mono.error(new BadRequestException("Order cannot be cancelled. Current status: " + currentStatus));
                    }

                    return runCompensationHandlers(entity.getOrderId())
                            .then(Mono.fromSupplier(() -> updateStatusAndTimestamps(entity, OrderStatus.CANCELLED)))
                            .flatMap(orderRepository::save)
                            .then(recordEvent(entity.getOrderId(), "ORDER_CANCELLED", OrderStatus.CANCELLED, "Order cancelled and compensations executed"))
                            .then();
                });
    }

    @Override
    public Mono<Order> updateOrderStatus(String orderId, OrderStatus status) {
        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .flatMap(entity -> {
                    OrderStatus currentStatus = OrderStatus.valueOf(entity.getStatus());
                    if (!isValidStatusTransition(currentStatus, status)) {
                        return Mono.error(new BadRequestException("Invalid status transition from " + currentStatus + " to " + status));
                    }

                    updateStatusAndTimestamps(entity, status);

                    return orderRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .flatMap(saved -> recordEvent(saved.getOrderId(), "STATUS_UPDATED", status,
                                    "Status changed from " + currentStatus + " to " + status).thenReturn(saved))
                            .map(orderMapper::entityToApi)
                            .flatMap(this::attachOrderItems);
                });
    }

    @Override
    public Mono<Order> updatePaymentStatus(String orderId, String paymentStatus) {
        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .flatMap(entity -> {
                    entity.setPaymentStatus(paymentStatus);
                    entity.setUpdatedAt(LocalDateTime.now().toString());
                    return orderRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .flatMap(saved -> recordEvent(saved.getOrderId(), "PAYMENT_STATUS_UPDATED", OrderStatus.valueOf(saved.getStatus()),
                                    "Payment status set to " + paymentStatus).thenReturn(saved))
                            .map(orderMapper::entityToApi)
                            .flatMap(this::attachOrderItems);
                });
    }

    @Override
    public Mono<Order> updateShippingInfo(String orderId, String trackingNumber, String carrier) {
        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .flatMap(entity -> {
                    entity.setTrackingNumber(trackingNumber);
                    entity.setCarrier(carrier);
                    entity.setUpdatedAt(LocalDateTime.now().toString());
                    return orderRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .flatMap(saved -> recordEvent(saved.getOrderId(), "SHIPPING_UPDATED", OrderStatus.valueOf(saved.getStatus()),
                                    "Tracking number updated to " + trackingNumber).thenReturn(saved))
                            .map(orderMapper::entityToApi)
                            .flatMap(this::attachOrderItems);
                });
    }

    @Override
    public Flux<OrderEvent> getOrderEvents(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .thenMany(orderEventRepository.findByOrderIdOrderByCreatedAtAsc(orderId)
                        .map(orderEventMapper::entityToApi));
    }

    private Mono<Void> validateNonEmptyCart(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return Mono.error(new BadRequestException("Cannot create order from an empty cart"));
        }
        return Mono.empty();
    }

    private Mono<Void> populateOrderFromCart(Order order, Cart cart) {
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(item -> new OrderItem(
                        UUID.randomUUID().toString(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductImage(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getDiscountAmount(),
                        item.getTotalPrice(),
                        "PENDING"
                ))
                .toList();

        order.setItems(orderItems);
        order.setSubtotal(cart.getSubtotal());
        order.setDiscountTotal(cart.getDiscountTotal());
        order.setTaxAmount(cart.getTaxAmount());
        order.setShippingCost(cart.getShippingCost());
        order.setGrandTotal(cart.getGrandTotal());
        order.setCreatedAt(LocalDateTime.now().toString());
        order.setUpdatedAt(LocalDateTime.now().toString());

        return Mono.empty();
    }

    private Mono<Void> reserveInventoryAllOrNothing(Order order, SagaState sagaState) {
        return Flux.fromIterable(order.getItems())
                .flatMap(item -> inventoryPort.reserveStock(item.getProductId(), item.getQuantity())
                        .doOnNext(sagaState::addReservation))
                .then();
    }

    private Mono<Void> authorizePayment(Order order, SagaState sagaState) {
        PaymentRequest request = new PaymentRequest(
                order.getOrderId(),
                order.getGrandTotal(),
                "USD",
                order.getPaymentMethod(),
                null,
                Boolean.FALSE,
                order.getBillingAddress()
        );

        return paymentPort.authorize(request)
                .flatMap(payment -> {
                    sagaState.setPayment(payment);
                    if (payment.getPaymentStatus() == null ||
                            !("COMPLETED".equalsIgnoreCase(payment.getPaymentStatus())
                                    || "AUTHORIZED".equalsIgnoreCase(payment.getPaymentStatus()))) {
                        return Mono.error(new BadRequestException("Payment authorization failed"));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> persistOrderAggregate(Order order) {
        OrderEntity entity = orderMapper.apiToEntity(order);
        entity.setStatus(order.getStatus().name());

        return orderRepository.save(entity)
                .onErrorMap(DataIntegrityViolationException.class,
                        ex -> new InvalidInputException("Duplicate order ID: " + order.getOrderId()))
                .flatMap(saved -> persistOrderItems(saved.getOrderId(), order.getItems())
                        .then(recordEvent(saved.getOrderId(), "ORDER_PERSISTED", order.getStatus(), "Order and order items persisted")));
    }

    private Mono<Void> persistOrderItems(String orderId, List<OrderItem> items) {
        return Flux.fromIterable(items)
                .map(orderItemMapper::apiToEntity)
                .map(entity -> {
                    entity.setOrderId(orderId);
                    return entity;
                })
                .flatMap(orderItemRepository::save)
                .then();
    }


    private Mono<Void> persistOrderStatus(Order order) {
        return orderRepository.findByOrderId(order.getOrderId())
                .flatMap(entity -> {
                    updateStatusAndTimestamps(entity, order.getStatus());
                    return orderRepository.save(entity);
                })
                .then(recordEvent(order.getOrderId(), "STATUS_PERSISTED", order.getStatus(), "Order status persisted"));
    }
    private Mono<Void> sendConfirmation(Order order) {
        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                order.getUserId(),
                "EMAIL",
                "ORDER_CONFIRMATION",
                "Order " + order.getOrderId() + " confirmed",
                "Your order has been placed successfully.",
                null,
                false,
                LocalDateTime.now(),
                null,
                "PENDING",
                null,
                LocalDateTime.now().toString()
        );

        return notificationPort.sendOrderConfirmation(notification)
                .flatMap(ignored -> recordEvent(order.getOrderId(), "ORDER_CONFIRMED", OrderStatus.CONFIRMED,
                        "Order confirmation sent"));
    }

    private Mono<Order> compensateSaga(Order order, SagaState sagaState, Throwable originalError) {
        LOG.warn("Order saga failed for orderId={}, triggering compensations", order.getOrderId(), originalError);

        return compensateInventory(sagaState)
                .then(compensatePayment(sagaState))
                .then(compensateShipment(order.getOrderId()))
                .then(markOrderFailed(order, originalError.getMessage()))
                .then(Mono.error(originalError));
    }

    private Mono<Void> compensateInventory(SagaState sagaState) {
        return Flux.fromIterable(sagaState.getReservations())
                .flatMap(reservation -> {
                    Mono<Void> cancelReservation = reservation.getReservationId() == null
                            ? Mono.empty()
                            : inventoryPort.cancelReservation(reservation.getReservationId()).then();
                    return cancelReservation
                            .then(inventoryPort.releaseReservedStock(reservation.getProductId(), reservation.getQuantity()))
                            .onErrorResume(ex -> Mono.empty());
                })
                .then(recordEvent(sagaState.getOrderId(), "INVENTORY_RELEASED", OrderStatus.FAILED,
                        "Released reserved inventory as compensation"))
                .onErrorResume(ex -> Mono.empty());
    }

    private Mono<Void> compensatePayment(SagaState sagaState) {
        Payment payment = sagaState.getPayment();
        if (payment == null || payment.getPaymentId() == null) {
            return Mono.empty();
        }

        return paymentPort.refund(payment.getPaymentId(), payment.getAmount(), "Order compensation")
                .then(recordEvent(sagaState.getOrderId(), "PAYMENT_REFUNDED", OrderStatus.REFUNDED,
                        "Payment void/refund executed as compensation"))
                .onErrorResume(ex -> Mono.empty());
    }

    private Mono<Void> compensateShipment(String orderId) {
        return shippingPort.getShipmentByOrderId(orderId)
                .flatMap(shipment -> shippingPort.cancelShipment(shipment.getShipmentId()).then())
                .then(recordEvent(orderId, "SHIPMENT_CANCELLED", OrderStatus.CANCELLED,
                        "Shipment cancellation executed as compensation"))
                .onErrorResume(ex -> Mono.empty());
    }

    private Mono<Void> runCompensationHandlers(String orderId) {
        SagaState state = new SagaState(orderId);
        return inventoryPort.getReservationsByOrderId(orderId)
                .doOnNext(state::addReservation)
                .then(Mono.defer(() -> compensateInventory(state)
                        .then(fetchPaymentForOrder(orderId).doOnNext(state::setPayment).then(compensatePayment(state)))
                        .then(compensateShipment(orderId))));
    }

    private Mono<Payment> fetchPaymentForOrder(String orderId) {
        return paymentPort.getPaymentByOrderId(orderId).onErrorResume(ex -> Mono.empty());
    }

    private Mono<Void> markOrderFailed(Order order, String reason) {
        return orderRepository.findByOrderId(order.getOrderId())
                .flatMap(entity -> {
                    updateStatusAndTimestamps(entity, OrderStatus.FAILED);
                    entity.setPaymentStatus("FAILED");
                    return orderRepository.save(entity);
                })
                .then(recordEvent(order.getOrderId(), "ORDER_FAILED", OrderStatus.FAILED, reason))
                .switchIfEmpty(recordEvent(order.getOrderId(), "ORDER_FAILED", OrderStatus.FAILED, reason));
    }

    private Mono<Void> changeStatus(Order order, OrderStatus targetStatus, String message) {
        if (!isValidStatusTransition(order.getStatus(), targetStatus)) {
            return Mono.error(new BadRequestException("Invalid status transition from " + order.getStatus() + " to " + targetStatus));
        }

        order.setStatus(targetStatus);
        if (targetStatus == OrderStatus.CONFIRMED) {
            order.setConfirmedDate(LocalDateTime.now());
        } else if (targetStatus == OrderStatus.SHIPPED) {
            order.setShippedDate(LocalDateTime.now());
        } else if (targetStatus == OrderStatus.DELIVERED) {
            order.setDeliveredDate(LocalDateTime.now());
        }

        return recordEvent(order.getOrderId(), "STATUS_UPDATED", targetStatus, message);
    }

    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == null) {
            return newStatus == OrderStatus.PENDING;
        }
        return STATUS_TRANSITIONS.getOrDefault(currentStatus, EnumSet.noneOf(OrderStatus.class)).contains(newStatus);
    }

    private OrderEntity updateStatusAndTimestamps(OrderEntity entity, OrderStatus status) {
        entity.setStatus(status.name());
        entity.setUpdatedAt(LocalDateTime.now().toString());
        switch (status) {
            case CONFIRMED -> entity.setConfirmedDate(LocalDateTime.now());
            case SHIPPED -> entity.setShippedDate(LocalDateTime.now());
            case DELIVERED -> entity.setDeliveredDate(LocalDateTime.now());
            default -> {
            }
        }
        return entity;
    }

    private Mono<Order> attachOrderItems(Order order) {
        return orderItemRepository.findByOrderId(order.getOrderId())
                .map(orderItemMapper::entityToApi)
                .collectList()
                .map(items -> {
                    order.setItems(items);
                    return order;
                });
    }

    private Mono<Void> recordEvent(String orderId, String eventType, OrderStatus status, String message) {
        OrderEventEntity event = new OrderEventEntity(
                null,
                UUID.randomUUID().toString(),
                orderId,
                eventType,
                status.name(),
                message,
                LocalDateTime.now()
        );
        return orderEventRepository.save(event).then();
    }
}
