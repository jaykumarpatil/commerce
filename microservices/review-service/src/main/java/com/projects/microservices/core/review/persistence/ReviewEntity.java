package com.projects.microservices.core.review.persistence;

import static java.lang.String.format;

import com.projects.api.core.review.ModerationStatus;
import com.projects.api.core.review.ReviewStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reviews", indexes = {
  @Index(name = "reviews_unique_idx", unique = true, columnList = "productId,userId"),
  @Index(name = "reviews_status_idx", columnList = "productId,moderationStatus")
})
public class ReviewEntity {

  @Id @GeneratedValue
  private int id;

  @Version
  private int version;

  private int productId;
  private int reviewId;
  private String userId;
  private String author;
  private String subject;
  private String content;
  private int rating;

  @Enumerated(EnumType.STRING)
  private ModerationStatus moderationStatus;

  @Enumerated(EnumType.STRING)
  private ReviewStatus status;

  private Instant createdAt;
  private Instant updatedAt;

  public ReviewEntity() {
  }

  public ReviewEntity(int productId, int reviewId, String userId, String author, String subject, String content,
                      ModerationStatus moderationStatus, Instant createdAt, Instant updatedAt) {
    this(productId, reviewId, userId, author, subject, content, 5, moderationStatus, ReviewStatus.PENDING_MODERATION, createdAt, updatedAt);
  }

  public ReviewEntity(int productId, int reviewId, String userId, String author, String subject, String content,
                      int rating, ModerationStatus moderationStatus, ReviewStatus status, Instant createdAt, Instant updatedAt) {
    this.productId = productId;
    this.reviewId = reviewId;
    this.userId = userId;
    this.author = author;
    this.subject = subject;
    this.content = content;
    this.rating = rating;
    this.moderationStatus = moderationStatus;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    if (createdAt == null) createdAt = now;
    if (updatedAt == null) updatedAt = now;
    if (moderationStatus == null) moderationStatus = ModerationStatus.PENDING;
    if (status == null) status = ReviewStatus.PENDING_MODERATION;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }

  @Override
  public String toString() {
    return format("ReviewEntity: %s/%d", productId, reviewId);
  }

  public int getId() { return id; }
  public void setId(int id) { this.id = id; }
  public int getVersion() { return version; }
  public void setVersion(int version) { this.version = version; }
  public int getProductId() { return productId; }
  public void setProductId(int productId) { this.productId = productId; }
  public int getReviewId() { return reviewId; }
  public void setReviewId(int reviewId) { this.reviewId = reviewId; }
  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }
  public String getAuthor() { return author; }
  public void setAuthor(String author) { this.author = author; }
  public String getSubject() { return subject; }
  public void setSubject(String subject) { this.subject = subject; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public int getRating() { return rating; }
  public void setRating(int rating) { this.rating = rating; }
  public ModerationStatus getModerationStatus() { return moderationStatus; }
  public void setModerationStatus(ModerationStatus moderationStatus) { this.moderationStatus = moderationStatus; }
  public ReviewStatus getStatus() { return status; }
  public void setStatus(ReviewStatus status) { this.status = status; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
