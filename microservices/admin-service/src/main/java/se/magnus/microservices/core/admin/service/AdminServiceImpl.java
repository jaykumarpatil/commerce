package se.magnus.microservices.core.admin.service;

import static java.util.logging.Level.FINE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.admin.*;
import se.magnus.api.exceptions.BadRequestException;
import se.magnus.microservices.core.admin.persistence.*;

@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger LOG = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final AdminUserRepository adminUserRepository;
    private final BannerRepository bannerRepository;
    private final CouponRepository couponRepository;
    private final AdminUserMapper adminUserMapper;
    private final BannerMapper bannerMapper;
    private final CouponMapper couponMapper;

    @Autowired
    public AdminServiceImpl(AdminUserRepository adminUserRepository,
                           BannerRepository bannerRepository,
                           CouponRepository couponRepository,
                           AdminUserMapper adminUserMapper,
                           BannerMapper bannerMapper,
                           CouponMapper couponMapper) {
        this.adminUserRepository = adminUserRepository;
        this.bannerRepository = bannerRepository;
        this.couponRepository = couponRepository;
        this.adminUserMapper = adminUserMapper;
        this.bannerMapper = bannerMapper;
        this.couponMapper = couponMapper;
    }

    @Override
    public Flux<AdminUser> getAllUsers() {
        return adminUserRepository.findAll()
                .map(adminUserMapper::entityToApi);
    }

    @Override
    public Mono<AdminUser> updateUserRole(String userId, String role) {
        return adminUserRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new BadRequestException("User not found: " + userId)))
                .flatMap(entity -> {
                    entity.setRole(role);
                    return adminUserRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(adminUserMapper::entityToApi);
                });
    }

    @Override
    public Mono<AdminUser> updateUserStatus(String userId, boolean enabled) {
        return adminUserRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new BadRequestException("User not found: " + userId)))
                .flatMap(entity -> {
                    entity.setEnabled(enabled);
                    return adminUserRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(adminUserMapper::entityToApi);
                });
    }

    @Override
    public Mono<Banner> createBanner(Banner banner) {
        BannerEntity entity = bannerMapper.apiToEntity(banner);
        
        return bannerRepository.save(entity)
                .log(LOG.getName(), FINE)
                .map(bannerMapper::entityToApi);
    }

    @Override
    public Flux<Banner> getAllBanners() {
        return bannerRepository.findAll()
                .map(bannerMapper::entityToApi);
    }

    @Override
    public Mono<Banner> updateBanner(String bannerId, Banner banner) {
        return bannerRepository.findByBannerId(bannerId)
                .switchIfEmpty(Mono.error(new BadRequestException("Banner not found: " + bannerId)))
                .flatMap(entity -> {
                    if (banner.getTitle() != null) entity.setTitle(banner.getTitle());
                    if (banner.getDescription() != null) entity.setDescription(banner.getDescription());
                    if (banner.getImageUrl() != null) entity.setImageUrl(banner.getImageUrl());
                    if (banner.getLinkUrl() != null) entity.setLinkUrl(banner.getLinkUrl());
                    if (banner.getSortOrder() != null) entity.setSortOrder(banner.getSortOrder());
                    entity.setActive(banner.isActive());

                    return bannerRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(bannerMapper::entityToApi);
                });
    }

    @Override
    public Mono<Void> deleteBanner(String bannerId) {
        return bannerRepository.findByBannerId(bannerId)
                .switchIfEmpty(Mono.error(new BadRequestException("Banner not found: " + bannerId)))
                .flatMap(bannerRepository::delete)
                .then(Mono.empty());
    }

    @Override
    public Mono<Coupon> createCoupon(Coupon coupon) {
        CouponEntity entity = couponMapper.apiToEntity(coupon);
        
        return couponRepository.save(entity)
                .log(LOG.getName(), FINE)
                .map(couponMapper::entityToApi);
    }

    @Override
    public Flux<Coupon> getAllCoupons() {
        return couponRepository.findAll()
                .map(couponMapper::entityToApi);
    }

    @Override
    public Mono<Coupon> updateCoupon(String couponId, Coupon coupon) {
        return couponRepository.findByCouponId(couponId)
                .switchIfEmpty(Mono.error(new BadRequestException("Coupon not found: " + couponId)))
                .flatMap(entity -> {
                    if (coupon.getCode() != null) entity.setCode(coupon.getCode());
                    if (coupon.getDescription() != null) entity.setDescription(coupon.getDescription());
                    if (coupon.getDiscountPercent() != null) entity.setDiscountPercent(coupon.getDiscountPercent());
                    if (coupon.getMinimumOrderAmount() != null) entity.setMinimumOrderAmount(coupon.getMinimumOrderAmount());
                    if (coupon.getMaxUses() != null) entity.setMaxUses(coupon.getMaxUses());
                    entity.setActive(coupon.isActive());

                    return couponRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(couponMapper::entityToApi);
                });
    }

    @Override
    public Mono<Void> deleteCoupon(String couponId) {
        return couponRepository.findByCouponId(couponId)
                .switchIfEmpty(Mono.error(new BadRequestException("Coupon not found: " + couponId)))
                .flatMap(couponRepository::delete)
                .then(Mono.empty());
    }

    @Override
    public Mono<AnalyticsReport> getSalesReport() {
        // Mock analytics report
        AnalyticsReport report = new AnalyticsReport();
        report.setReportType("SALES");
        report.setStartDate(java.time.LocalDateTime.now().minusDays(30));
        report.setEndDate(java.time.LocalDateTime.now());
        report.setTotalValue(10000);
        report.setAverageValue(50.0);
        report.setPeriod("MONTH");
        
        return Mono.just(report);
    }

    @Override
    public Mono<AnalyticsReport> getOrdersReport() {
        // Mock analytics report
        AnalyticsReport report = new AnalyticsReport();
        report.setReportType("ORDERS");
        report.setStartDate(java.time.LocalDateTime.now().minusDays(30));
        report.setEndDate(java.time.LocalDateTime.now());
        report.setTotalValue(250);
        report.setAverageValue(40.0);
        report.setPeriod("MONTH");
        
        return Mono.just(report);
    }

    @Override
    public Mono<AnalyticsReport> getUsersReport() {
        // Mock analytics report
        AnalyticsReport report = new AnalyticsReport();
        report.setReportType("USERS");
        report.setStartDate(java.time.LocalDateTime.now().minusDays(30));
        report.setEndDate(java.time.LocalDateTime.now());
        report.setTotalValue(150);
        report.setAverageValue(2.5);
        report.setPeriod("MONTH");
        
        return Mono.just(report);
    }
}
