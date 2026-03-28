package com.projects.promotion.service;

import com.projects.promotion.domain.Coupon;
import java.util.List;

// Promotion MVP service contract
public interface PromotionService {
  List<Coupon> listCoupons();
  PromotionResult applyCoupon(double cartTotal, String couponCode, String userId);
  
  // Simple result wrapper for coupon application
  class PromotionResult {
    public final boolean success;
    public final double discount;
    public final double newTotal;
    public final String couponCode;
    public final String message;

    public PromotionResult(boolean success, double discount, double newTotal, String couponCode, String message) {
      this.success = success;
      this.discount = discount;
      this.newTotal = newTotal;
      this.couponCode = couponCode;
      this.message = message;
    }
  }
}
