package com.projects.api.core.review;

public class Review {
  private int productId;
  private int reviewId;
  private String author;
  private String subject;
  private String content;
  private String serviceAddress;
  private ReviewStatus status;

  public Review() {
    productId = 0;
    reviewId = 0;
    author = null;
    subject = null;
    content = null;
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
    this.author = author;
    this.subject = subject;
    this.content = content;
    this.serviceAddress = serviceAddress;
    this.status = status == null ? ReviewStatus.PENDING_MODERATION : status;
  }

  public int getProductId() {
    return productId;
  }

  public int getReviewId() {
    return reviewId;
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

  public void setAuthor(String author) {
    this.author = author;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setServiceAddress(String serviceAddress) {
    this.serviceAddress = serviceAddress;
  }

  public void setStatus(ReviewStatus status) {
    this.status = status;
  }
}
