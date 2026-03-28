package com.projects.microservices.cart.domain.service;

import com.projects.microservices.cart.domain.model.CartEntity;
import reactor.core.publisher.Mono;

public class CartDomainService {

    private static final double TAX_RATE = 0.10;
    private static final double FREE_SHIPPING_THRESHOLD = 50.0;
    private static final double SHIPPING_COST = 9.99;

    public Mono<CartEntity> calculateTotals(CartEntity cart) {
        double subtotal = 0;
        int itemCount = 0;

        if (cart.getItems() != null) {
            for (var item : cart.getItems()) {
                double itemTotal = item.calculateTotalPrice();
                subtotal += itemTotal;
                itemCount += item.getQuantity() != null ? item.getQuantity() : 0;
            }
        }

        double discountTotal = cart.getDiscountTotal() != null ? cart.getDiscountTotal() : 0;
        double taxableAmount = subtotal - discountTotal;
        double taxAmount = taxableAmount * TAX_RATE;
        double shippingCost = subtotal >= FREE_SHIPPING_THRESHOLD ? 0 : SHIPPING_COST;
        double grandTotal = taxableAmount + taxAmount + shippingCost;

        cart.setSubtotal(round(subtotal));
        cart.setDiscountTotal(round(discountTotal));
        cart.setTaxAmount(round(taxAmount));
        cart.setShippingCost(round(shippingCost));
        cart.setGrandTotal(round(grandTotal));
        cart.setItemTotalCount(itemCount);
        cart.setUpdatedAt(java.time.ZonedDateTime.now().toString());

        return Mono.just(cart);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
