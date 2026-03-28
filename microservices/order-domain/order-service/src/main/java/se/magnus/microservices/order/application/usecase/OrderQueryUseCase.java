package se.magnus.microservices.order.application.usecase;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.order.Order;
import se.magnus.api.core.order.OrderItem;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.microservices.order.application.port.inbound.OrderQueryPort;
import se.magnus.microservices.order.application.port.outbound.OrderRepositoryPort;
import se.magnus.microservices.order.domain.model.OrderEntity;
import se.magnus.microservices.order.domain.model.OrderItemEntity;

@Service
public class OrderQueryUseCase implements OrderQueryPort {

    private final OrderRepositoryPort repository;

    public OrderQueryUseCase(OrderRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Order> getOrder(String orderId) {
        return repository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("Order not found: " + orderId)))
                .map(this::mapToApi);
    }

    @Override
    public Flux<Order> getOrdersByUserId(String userId) {
        return repository.findByUserId(userId)
                .map(this::mapToApi);
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
