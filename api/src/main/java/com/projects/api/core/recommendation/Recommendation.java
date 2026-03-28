package com.projects.api.core.recommendation;

import java.time.Instant;

public class Recommendation {
  private int productId;
  private int recommendationId;
  private String author;
  private int rate;
  private String content;
  private String userId;
  private Double personalizationScore;
  private String scoreReason;
  private Instant generatedAt;
  private String serviceAddress;

  public Recommendation() {
    productId = 0;
    recommendationId = 0;
    author = null;
    rate = 0;
    content = null;
    userId = null;
    personalizationScore = null;
    scoreReason = null;
    generatedAt = null;
    serviceAddress = null;
  }

  public Recommendation(int productId, int recommendationId, String author, int rate, String content, String serviceAddress) {
    this(productId, recommendationId, author, rate, content, null, null, null, null, serviceAddress);
  }

  public Recommendation(int productId, int recommendationId, String author, int rate, String content,
                        String userId, Double personalizationScore, String scoreReason, Instant generatedAt,
                        String serviceAddress) {
    this.productId = productId;
    this.recommendationId = recommendationId;
    this.author = author;
    this.rate = rate;
    this.content = content;
    this.userId = userId;
    this.personalizationScore = personalizationScore;
    this.scoreReason = scoreReason;
    this.generatedAt = generatedAt;
    this.serviceAddress = serviceAddress;
  }

  public int getProductId() { return productId; }
  public int getRecommendationId() { return recommendationId; }
  public String getAuthor() { return author; }
  public int getRate() { return rate; }
  public String getContent() { return content; }
  public String getUserId() { return userId; }
  public Double getPersonalizationScore() { return personalizationScore; }
  public String getScoreReason() { return scoreReason; }
  public Instant getGeneratedAt() { return generatedAt; }
  public String getServiceAddress() { return serviceAddress; }

  public void setProductId(int productId) { this.productId = productId; }
  public void setRecommendationId(int recommendationId) { this.recommendationId = recommendationId; }
  public void setAuthor(String author) { this.author = author; }
  public void setRate(int rate) { this.rate = rate; }
  public void setContent(String content) { this.content = content; }
  public void setUserId(String userId) { this.userId = userId; }
  public void setPersonalizationScore(Double personalizationScore) { this.personalizationScore = personalizationScore; }
  public void setScoreReason(String scoreReason) { this.scoreReason = scoreReason; }
  public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
  public void setServiceAddress(String serviceAddress) { this.serviceAddress = serviceAddress; }
}
