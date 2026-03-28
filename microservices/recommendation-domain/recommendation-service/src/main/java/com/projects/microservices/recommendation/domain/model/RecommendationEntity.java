package com.projects.microservices.recommendation.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "recommendations")
public class RecommendationEntity {
    @Id
    private String id;
    private String recommendationId;
    private String userId;
    private String productId;
    private Double score;
    private String algorithm;
}
