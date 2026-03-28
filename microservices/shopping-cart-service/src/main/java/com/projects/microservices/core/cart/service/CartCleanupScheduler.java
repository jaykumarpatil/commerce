package com.projects.microservices.core.cart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CartCleanupScheduler {

    private final CartServiceImpl cartService;

    @Autowired
    public CartCleanupScheduler(CartServiceImpl cartService) {
        this.cartService = cartService;
    }

    @Scheduled(cron = "${cart.expiration.cleanup-cron:0 0 2 * * *}")
    public void clearExpiredCarts() {
        cartService.clearExpiredCarts().subscribe();
    }
}
