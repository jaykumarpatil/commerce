package com.projects.microservices.core.order.service.model;

import com.projects.api.core.inventory.StockReservation;
import com.projects.api.core.payment.Payment;

import java.util.ArrayList;
import java.util.List;

public class SagaState {
    private final String orderId;
    private final List<StockReservation> reservations = new ArrayList<>();
    private Payment payment;

    public SagaState(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public List<StockReservation> getReservations() {
        return reservations;
    }

    public void addReservation(StockReservation reservation) {
        this.reservations.add(reservation);
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}
