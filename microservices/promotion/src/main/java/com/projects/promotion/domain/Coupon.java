package com.projects.promotion.domain;

import java.time.LocalDate;

public class Coupon {
  private final Long id;
  private final String code;
  private final String description;
  private final double discountValue;
  private final DiscountType discountType;
  private final LocalDate validFrom;
  private final LocalDate validTo;
  private final int maxUses;
  private int uses;

  public Coupon(Long id, String code, String description, double discountValue, DiscountType discountType,
                LocalDate validFrom, LocalDate validTo, int maxUses, int uses) {
    this.id = id;
    this.code = code;
    this.description = description;
    this.discountValue = discountValue;
    this.discountType = discountType;
    this.validFrom = validFrom;
    this.validTo = validTo;
    this.maxUses = maxUses;
    this.uses = uses;
  }

  public Long getId() { return id; }
  public String getCode() { return code; }
  public String getDescription() { return description; }
  public double getDiscountValue() { return discountValue; }
  public DiscountType getDiscountType() { return discountType; }
  public LocalDate getValidFrom() { return validFrom; }
  public LocalDate getValidTo() { return validTo; }
  public int getMaxUses() { return maxUses; }
  public int getUses() { return uses; }
  public void incrementUses() { this.uses++; }
  public boolean isExpired(LocalDate now) {
    return now.isBefore(validFrom) || now.isAfter(validTo);
  }
}
