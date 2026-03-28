package com.projects.promotion.api;

public class CouponApplyRequest {
  private double cartTotal;
  private String couponCode;
  private String userId;

  public CouponApplyRequest() {}
  public CouponApplyRequest(double cartTotal, String couponCode, String userId) {
    this.cartTotal = cartTotal; this.couponCode = couponCode; this.userId = userId;
  }
  public double getCartTotal() { return cartTotal; }
  public void setCartTotal(double cartTotal) { this.cartTotal = cartTotal; }
  public String getCouponCode() { return couponCode; }
  public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }
}
