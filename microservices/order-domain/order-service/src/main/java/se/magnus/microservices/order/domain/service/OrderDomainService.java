package se.magnus.microservices.order.domain.service;

import se.magnus.microservices.order.domain.model.OrderEntity;
import se.magnus.microservices.order.domain.model.OrderItemEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderDomainService {

    private static final double TAX_RATE = 0.10;
    private static final double FREE_SHIPPING_THRESHOLD = 50.0;
    private static final double SHIPPING_COST = 9.99;

    public OrderEntity calculateTotals(OrderEntity order) {
        double subtotal = 0;
        
        if (order.getItems() != null) {
            for (OrderItemEntity item : order.getItems()) {
                subtotal += item.calculateTotal();
            }
        }
        
        double discountTotal = order.getDiscountTotal() != null ? order.getDiscountTotal() : 0;
        double taxableAmount = subtotal - discountTotal;
        double taxAmount = round(taxableAmount * TAX_RATE);
        double shippingCost = subtotal >= FREE_SHIPPING_THRESHOLD ? 0 : SHIPPING_COST;
        double grandTotal = round(taxableAmount + taxAmount + shippingCost);
        
        order.setSubtotal(round(subtotal));
        order.setDiscountTotal(round(discountTotal));
        order.setTaxAmount(taxAmount);
        order.setShippingCost(shippingCost);
        order.setGrandTotal(grandTotal);
        order.setUpdatedAt(LocalDateTime.now().toString());
        
        return order;
    }

    public boolean canTransitionStatus(OrderEntity order, String newStatus) {
        String currentStatus = order.getStatus();
        
        return switch (currentStatus) {
            case "PENDING" -> "CONFIRMED".equals(newStatus) || "CANCELLED".equals(newStatus);
            case "CONFIRMED" -> "PROCESSING".equals(newStatus) || "CANCELLED".equals(newStatus);
            case "PROCESSING" -> "SHIPPED".equals(newStatus);
            case "SHIPPED" -> "DELIVERED".equals(newStatus);
            case "DELIVERED", "CANCELLED" -> false;
            default -> false;
        };
    }

    public OrderEntity applyStatusChange(OrderEntity order, String newStatus) {
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now().toString());
        
        switch (newStatus) {
            case "CONFIRMED" -> order.setConfirmedDate(LocalDateTime.now());
            case "SHIPPED" -> order.setShippedDate(LocalDateTime.now());
            case "DELIVERED" -> order.setDeliveredDate(LocalDateTime.now());
        }
        
        return order;
    }

    public boolean canCancel(OrderEntity order) {
        return "PENDING".equals(order.getStatus()) || "CONFIRMED".equals(order.getStatus());
    }

    public OrderEntity cancel(OrderEntity order, String reason) {
        if (!canCancel(order)) {
            throw new IllegalStateException("Order cannot be cancelled in status: " + order.getStatus());
        }
        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now().toString());
        return order;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
