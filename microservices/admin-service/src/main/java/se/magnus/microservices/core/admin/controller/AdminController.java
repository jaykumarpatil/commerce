package se.magnus.microservices.core.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.admin.*;
import se.magnus.util.http.ServiceUtil;

@RestController
public class AdminController implements AdminService {

    private final AdminService adminService;
    private final ServiceUtil serviceUtil;

    @Autowired
    public AdminController(AdminService adminService, ServiceUtil serviceUtil) {
        this.adminService = adminService;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Flux<AdminUser> getAllUsers() {
        return adminService.getAllUsers();
    }

    @Override
    public Mono<AdminUser> updateUserRole(String userId, String role) {
        return adminService.updateUserRole(userId, role);
    }

    @Override
    public Mono<AdminUser> updateUserStatus(String userId, boolean enabled) {
        return adminService.updateUserStatus(userId, enabled);
    }

    @Override
    public Mono<Banner> createBanner(Banner banner) {
        return adminService.createBanner(banner);
    }

    @Override
    public Flux<Banner> getAllBanners() {
        return adminService.getAllBanners();
    }

    @Override
    public Mono<Banner> updateBanner(String bannerId, Banner banner) {
        return adminService.updateBanner(bannerId, banner);
    }

    @Override
    public Mono<Void> deleteBanner(String bannerId) {
        return adminService.deleteBanner(bannerId);
    }

    @Override
    public Mono<Coupon> createCoupon(Coupon coupon) {
        return adminService.createCoupon(coupon);
    }

    @Override
    public Flux<Coupon> getAllCoupons() {
        return adminService.getAllCoupons();
    }

    @Override
    public Mono<Coupon> updateCoupon(String couponId, Coupon coupon) {
        return adminService.updateCoupon(couponId, coupon);
    }

    @Override
    public Mono<Void> deleteCoupon(String couponId) {
        return adminService.deleteCoupon(couponId);
    }

    @Override
    public Mono<AnalyticsReport> getSalesReport() {
        return adminService.getSalesReport();
    }

    @Override
    public Mono<AnalyticsReport> getOrdersReport() {
        return adminService.getOrdersReport();
    }

    @Override
    public Mono<AnalyticsReport> getUsersReport() {
        return adminService.getUsersReport();
    }
}
