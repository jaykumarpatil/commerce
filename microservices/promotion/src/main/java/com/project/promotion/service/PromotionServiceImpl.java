package com.project.promotion.service;

import com.project.promotion.domain.Coupon;
import com.project.promotion.domain.DiscountType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Simple in-memory implementation of PromotionService
public class PromotionServiceImpl implements PromotionService {
  private final List<Coupon> coupons = new ArrayList<>();

  public PromotionServiceImpl() {
    // Seed two coupons
    coupons.add(new Coupon(1L, "PROMO10", "10% off", 10.0, DiscountType.PERCENTAGE,
        LocalDate.now().minusDays(1), LocalDate.now().plusMonths(6), 100, 0));
    coupons.add(new Coupon(2L, "FLAT5", "$5 off", 5.0, DiscountType.FIXED,
        LocalDate.now().minusDays(1), LocalDate.now().plusMonths(6), 50, 0));
  }

  @Override
  public List<Coupon> listCoupons() {
    return coupons;
  }

  @Override
  public PromotionResult applyCoupon(double cartTotal, String couponCode, String userId) {
    Coupon coupon = coupons.stream()
        .filter(c -> c.getCode().equalsIgnoreCase(couponCode))
        .findFirst()
        .orElse(null);

    LocalDate today = LocalDate.now();
    if (coupon == null || coupon.isExpired(today) || coupon.getUses() >= coupon.getMaxUses()) {
      return new PromotionResult(false, 0.0, cartTotal, couponCode, "Coupon invalid or not usable");
    }

    double discount = 0.0;
    if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
      discount = cartTotal * (coupon.getDiscountValue() / 100.0);
    } else {
      discount = coupon.getDiscountValue();
    }
    double newTotal = cartTotal - discount;
    coupon.incrementUses();
    return new PromotionResult(true, discount, newTotal, couponCode, "Coupon applied");
  }
}
