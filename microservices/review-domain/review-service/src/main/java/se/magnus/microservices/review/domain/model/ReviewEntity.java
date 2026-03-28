package se.magnus.microservices.review.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reviews")
public class ReviewEntity {
    @Id
    private String id;
    private String reviewId;
    private String productId;
    private String userId;
    private Integer rating;
    private String title;
    private String comment;
    private String createdAt;
}
