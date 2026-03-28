package com.projects.microservices.core.recommendation.services;

import com.projects.api.core.recommendation.Recommendation;
import com.projects.microservices.core.recommendation.persistence.RecommendationEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RecommendationMapper {

  public Recommendation entityToApi(RecommendationEntity entity) {
    if (entity == null) {
      return null;
    }
    return new Recommendation(
        entity.getProductId(),
        entity.getRecommendationId(),
        entity.getAuthor(),
        entity.getRating(),
        entity.getContent(),
        entity.getUserId(),
        entity.getPersonalizationScore(),
        entity.getScoreReason(),
        entity.getGeneratedAt(),
        null);
  }

  public RecommendationEntity apiToEntity(Recommendation api) {
    if (api == null) {
      return null;
    }
    return new RecommendationEntity(
        api.getProductId(),
        api.getRecommendationId(),
        api.getAuthor(),
        api.getRate(),
        api.getContent(),
        api.getUserId(),
        api.getPersonalizationScore(),
        api.getScoreReason(),
        api.getGeneratedAt() == null ? Instant.now() : api.getGeneratedAt());
  }

  public List<Recommendation> entityListToApiList(List<RecommendationEntity> entityList) {
    if (entityList == null) {
      return List.of();
    }
    return entityList.stream().map(this::entityToApi).toList();
  }

  public List<RecommendationEntity> apiListToEntityList(List<Recommendation> apiList) {
    if (apiList == null) {
      return List.of();
    }
    return apiList.stream().map(this::apiToEntity).toList();
  }
}
