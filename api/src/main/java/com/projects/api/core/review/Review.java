package com.projects.api.core.review;

import java.time.Instant;

public class Review {
  private int productId;
  private int reviewId;
  private String userId;
  private String author;
  private String subject;
  private String content;
  private ModerationStatus moderationStatus;
  private Instant createdAt;
  private Instant updatedAt;
  private String serviceAddress;
  private ReviewStatus status;

  public Review() {
    productId = 0;
    reviewId = 0;
    userId = null;
    author = null;
    subject = null;
    content = null;
    moderationStatus = ModerationStatus.PENDING;
    createdAt = null;
    updatedAt = null;
    serviceAddress = null;
    status = ReviewStatus.PENDING_MODERATION;
  }

  public Review(
      int productId,
      int reviewId,
      String author,
      String subject,
      String content,
      String serviceAddress) {
    this(
        productId,
        reviewId,
        null,
        author,
        subject,
        content,
        ModerationStatus.PENDING,
        null,
        null,
        serviceAddress);
  }

  public Review(
      int productId,
      int reviewId,
      String userId,
      String author,
      String subject,
      String content,
      ModerationStatus moderationStatus,
      Instant createdAt,
      Instant updatedAt,
      String serviceAddress) {

    this(productId, reviewId, author, subject, content, serviceAddress, ReviewStatus.PENDING_MODERATION);
  }

  public Review(
    int productId,
    int reviewId,
    String author,
    String subject,
    String content,
    String serviceAddress,
    ReviewStatus status) {

    this.productId = productId;
    this.reviewId = reviewId;
    this.userId = userId;
    this.author = author;
    this.subject = subject;
    this.content = content;
    this.moderationStatus = moderationStatus;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.serviceAddress = serviceAddress;
    this.status = status == null ? ReviewStatus.PENDING_MODERATION : status;
  }

  public int getProductId() {
    return productId;
  }

  public int getReviewId() {
    return reviewId;
  }

  public String getUserId() {
    return userId;
  }

  public String getAuthor() {
    return author;
  }

  public String getSubject() {
    return subject;
  }

  public String getContent() {
    return content;
  }

  public ModerationStatus getModerationStatus() {
    return moderationStatus;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public String getServiceAddress() {
    return serviceAddress;
  }

  public ReviewStatus getStatus() {
    return status;
  }

  public void setProductId(int productId) {
    this.productId = productId;
  }

  public void setReviewId(int reviewId) {
    this.reviewId = reviewId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setModerationStatus(ModerationStatus moderationStatus) {
    this.moderationStatus = moderationStatus;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void setServiceAddress(String serviceAddress) {
    this.serviceAddress = serviceAddress;
  }

  public void setStatus(ReviewStatus status) {
    this.status = status;
  }
}
