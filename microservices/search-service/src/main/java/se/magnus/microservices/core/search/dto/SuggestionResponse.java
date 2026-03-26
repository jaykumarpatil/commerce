package se.magnus.microservices.core.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionResponse {
    private String text;
    private String productId;
    private String categoryId;
    private String imageUrl;
    private Double price;
    private Float score;
}
