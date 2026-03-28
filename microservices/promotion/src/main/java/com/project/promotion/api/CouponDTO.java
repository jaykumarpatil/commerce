package com.project.promotion.api;

import com.project.promotion.domain.DiscountType;

public class CouponDTO {
  private String code;
  private String description;
  private double discountValue;
  private String discountType;
  private String validFrom;
  private String validTo;
  private int maxUses;
  private int uses;

  public CouponDTO(String code, String description, double discountValue, String discountType,
                   String validFrom, String validTo, int maxUses, int uses) {
    this.code = code;
    this.description = description;
    this.discountValue = discountValue;
    this.discountType = discountType;
    this.validFrom = validFrom;
    this.validTo = validTo;
    this.maxUses = maxUses;
    this.uses = uses;
  }
  public String getCode() { return code; }
  public String getDescription() { return description; }
  public double getDiscountValue() { return discountValue; }
  public String getDiscountType() { return discountType; }
  public String getValidFrom() { return validFrom; }
  public String getValidTo() { return validTo; }
  public int getMaxUses() { return maxUses; }
  public int getUses() { return uses; }
}
