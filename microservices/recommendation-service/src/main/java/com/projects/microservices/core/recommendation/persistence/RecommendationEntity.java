package com.projects.microservices.core.recommendation.persistence;

import static java.lang.String.format;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "recommendations")
@CompoundIndex(name = "prod-rec-id", unique = true, def = "{'productId': 1, 'recommendationId' : 1}")
public class RecommendationEntity {

  @Id
  private String id;

  @Version
  private Integer version;

  private int productId;
  private int recommendationId;
  private String author;
  private int rating;
  private String content;
  private String userId;
  private Double personalizationScore;
  private String scoreReason;
  private Instant generatedAt;

  public RecommendationEntity() {
  }


  public RecommendationEntity(int productId, int recommendationId, String author, int rating, String content) {
    this(productId, recommendationId, author, rating, content, null, null, null, Instant.now());
  }

  public RecommendationEntity(int productId, int recommendationId, String author, int rating, String content,
                              String userId, Double personalizationScore, String scoreReason, Instant generatedAt) {
    this.productId = productId;
    this.recommendationId = recommendationId;
    this.author = author;
    this.rating = rating;
    this.content = content;
    this.userId = userId;
    this.personalizationScore = personalizationScore;
    this.scoreReason = scoreReason;
    this.generatedAt = generatedAt;
  }

  @Override
  public String toString() { return format("RecommendationEntity: %s/%d", productId, recommendationId); }

  public String getId() { return id; }
  public Integer getVersion() { return version; }
  public int getProductId() { return productId; }
  public int getRecommendationId() { return recommendationId; }
  public String getAuthor() { return author; }
  public int getRating() { return rating; }
  public String getContent() { return content; }
  public String getUserId() { return userId; }
  public Double getPersonalizationScore() { return personalizationScore; }
  public String getScoreReason() { return scoreReason; }
  public Instant getGeneratedAt() { return generatedAt; }

  public void setId(String id) { this.id = id; }
  public void setVersion(Integer version) { this.version = version; }
  public void setProductId(int productId) { this.productId = productId; }
  public void setRecommendationId(int recommendationId) { this.recommendationId = recommendationId; }
  public void setAuthor(String author) { this.author = author; }
  public void setRating(int rating) { this.rating = rating; }
  public void setContent(String content) { this.content = content; }
  public void setUserId(String userId) { this.userId = userId; }
  public void setPersonalizationScore(Double personalizationScore) { this.personalizationScore = personalizationScore; }
  public void setScoreReason(String scoreReason) { this.scoreReason = scoreReason; }
  public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
}
