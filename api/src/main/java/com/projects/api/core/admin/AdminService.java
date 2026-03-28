package com.projects.api.core.admin;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdminService {

    // User Management
    @GetMapping("/v1/admin/users")
    Flux<AdminUser> getAllUsers();

    @PatchMapping("/v1/admin/users/{userId}/role")
    Mono<AdminUser> updateUserRole(String userId, String role);

    @PatchMapping("/v1/admin/users/{userId}/status")
    Mono<AdminUser> updateUserStatus(String userId, boolean enabled);

    // Banner Management
    @PostMapping("/v1/admin/banners")
    Mono<Banner> createBanner(Banner banner);

    @GetMapping("/v1/admin/banners")
    Flux<Banner> getAllBanners();

    @PutMapping("/v1/admin/banners/{bannerId}")
    Mono<Banner> updateBanner(String bannerId, Banner banner);

    @DeleteMapping("/v1/admin/banners/{bannerId}")
    Mono<Void> deleteBanner(String bannerId);

    // Coupon Management
    @PostMapping("/v1/admin/coupons")
    Mono<Coupon> createCoupon(Coupon coupon);

    @GetMapping("/v1/admin/coupons")
    Flux<Coupon> getAllCoupons();

    @PutMapping("/v1/admin/coupons/{couponId}")
    Mono<Coupon> updateCoupon(String couponId, Coupon coupon);

    @DeleteMapping("/v1/admin/coupons/{couponId}")
    Mono<Void> deleteCoupon(String couponId);

    // Analytics
    @GetMapping("/v1/admin/analytics/sales")
    Mono<AnalyticsReport> getSalesReport();

    @GetMapping("/v1/admin/analytics/orders")
    Mono<AnalyticsReport> getOrdersReport();

    @GetMapping("/v1/admin/analytics/users")
    Mono<AnalyticsReport> getUsersReport();
}
