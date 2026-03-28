package com.projects.microservices.core.review.services;

import com.projects.api.core.review.ModerationStatus;
import com.projects.api.core.review.Review;
import com.projects.api.core.review.ReviewStatus;
import com.projects.microservices.core.review.persistence.ReviewEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

  public Review entityToApi(ReviewEntity entity) {
    if (entity == null) {
      return null;
    }
    return new Review(
        entity.getProductId(),
        entity.getReviewId(),
        entity.getUserId(),
        entity.getAuthor(),
        entity.getSubject(),
        entity.getContent(),
        entity.getRating(),
        entity.getModerationStatus(),
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        null,
        entity.getStatus());
  }

  public ReviewEntity apiToEntity(Review api) {
    if (api == null) {
      return null;
    }
    Instant now = Instant.now();
    ModerationStatus moderationStatus = api.getModerationStatus() == null ? ModerationStatus.PENDING : api.getModerationStatus();
    ReviewStatus reviewStatus = api.getStatus() == null ? ReviewStatus.PENDING_MODERATION : api.getStatus();
    return new ReviewEntity(
        api.getProductId(),
        api.getReviewId(),
        api.getUserId(),
        api.getAuthor(),
        api.getSubject(),
        api.getContent(),
        api.getRating(),
        moderationStatus,
        reviewStatus,
        api.getCreatedAt() == null ? now : api.getCreatedAt(),
        api.getUpdatedAt() == null ? now : api.getUpdatedAt());
  }

  public List<Review> entityListToApiList(List<ReviewEntity> entityList) {
    if (entityList == null) {
      return List.of();
    }
    return entityList.stream().map(this::entityToApi).toList();
  }

  public List<ReviewEntity> apiListToEntityList(List<Review> apiList) {
    if (apiList == null) {
      return List.of();
    }
    return apiList.stream().map(this::apiToEntity).toList();
  }
}
