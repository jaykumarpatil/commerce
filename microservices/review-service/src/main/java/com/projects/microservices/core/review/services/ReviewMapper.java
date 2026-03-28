package com.projects.microservices.core.review.services;

import com.projects.api.core.review.Review;
import com.projects.microservices.core.review.persistence.ReviewEntity;
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
        entity.getAuthor(),
        entity.getSubject(),
        entity.getContent(),
        null);
  }

  public ReviewEntity apiToEntity(Review api) {
    if (api == null) {
      return null;
    }
    return new ReviewEntity(
        api.getProductId(),
        api.getReviewId(),
        api.getAuthor(),
        api.getSubject(),
        api.getContent());
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
