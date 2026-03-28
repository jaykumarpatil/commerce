package com.project.promotion.adapter.inbound;

import com.project.promotion.domain.Coupon;
import com.project.promotion.domain.DiscountType;
import com.project.promotion.service.PromotionService;
import com.project.promotion.api.CouponDTO;
import com.project.promotion.api.CouponApplyRequest;
import com.project.promotion.service.PromotionServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/com.project/promotion")
public class PromotionController {
  private final PromotionService promotionService;

  public PromotionController() {
    // Simple default; in a real app we would autowire an implementation
    this.promotionService = new PromotionServiceImpl();
  }

  @GetMapping("/coupons")
  public List<CouponDTO> listCoupons() {
    List<Coupon> coupons = promotionService.listCoupons();
    return coupons.stream().map(c -> new CouponDTO(
        c.getCode(), c.getDescription(), c.getDiscountValue(), c.getDiscountType().name(),
        c.getValidFrom() != null ? c.getValidFrom().toString() : null,
        c.getValidTo() != null ? c.getValidTo().toString() : null,
        c.getMaxUses(), c.getUses()
    )).collect(Collectors.toList());
  }

  @PostMapping("/apply")
  public PromotionController.CouponApplyResponse applyCoupon(@RequestBody CouponApplyRequest req) {
    var result = promotionService.applyCoupon(req.getCartTotal(), req.getCouponCode(), req.getUserId());
    return new CouponApplyResponse(result.success, result.discount, result.newTotal, result.couponCode, result.message);
  }

  // DTO for endpoint response; defined here for simplicity
  public static class CouponApplyResponse {
    private boolean success;
    private double discount;
    private double newTotal;
    private String couponCode;
    private String message;

    public CouponApplyResponse(boolean success, double discount, double newTotal, String couponCode, String message) {
      this.success = success;
      this.discount = discount;
      this.newTotal = newTotal;
      this.couponCode = couponCode;
      this.message = message;
    }
    public boolean isSuccess() { return success; }
    public double getDiscount() { return discount; }
    public double getNewTotal() { return newTotal; }
    public String getCouponCode() { return couponCode; }
    public String getMessage() { return message; }
  }
}
