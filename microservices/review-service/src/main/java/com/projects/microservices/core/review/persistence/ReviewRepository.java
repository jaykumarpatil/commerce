package com.projects.microservices.core.review.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import com.projects.api.core.review.ModerationStatus;

public interface ReviewRepository extends CrudRepository<ReviewEntity, Integer> {

  @Transactional(readOnly = true)
  List<ReviewEntity> findByProductIdAndModerationStatus(int productId, ModerationStatus moderationStatus);

  @Transactional(readOnly = true)
  List<ReviewEntity> findByProductId(int productId);

  @Transactional(readOnly = true)
  Optional<ReviewEntity> findByProductIdAndReviewId(int productId, int reviewId);
}
