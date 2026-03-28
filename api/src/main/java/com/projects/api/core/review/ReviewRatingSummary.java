package com.projects.api.core.review;

import java.util.Map;

public class ReviewRatingSummary {
  private int productId;
  private double averageRating;
  private long totalReviews;
  private Map<Integer, Long> countByRating;

  public ReviewRatingSummary() {
  }

  public ReviewRatingSummary(int productId, double averageRating, long totalReviews, Map<Integer, Long> countByRating) {
    this.productId = productId;
    this.averageRating = averageRating;
    this.totalReviews = totalReviews;
    this.countByRating = countByRating;
  }

  public int getProductId() {
    return productId;
  }

  public void setProductId(int productId) {
    this.productId = productId;
  }

  public double getAverageRating() {
    return averageRating;
  }

  public void setAverageRating(double averageRating) {
    this.averageRating = averageRating;
  }

  public long getTotalReviews() {
    return totalReviews;
  }

  public void setTotalReviews(long totalReviews) {
    this.totalReviews = totalReviews;
  }

  public Map<Integer, Long> getCountByRating() {
    return countByRating;
  }

  public void setCountByRating(Map<Integer, Long> countByRating) {
    this.countByRating = countByRating;
  }
}
