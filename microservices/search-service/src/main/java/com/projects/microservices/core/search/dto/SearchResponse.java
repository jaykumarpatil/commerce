package com.projects.microservices.core.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private List<SearchHit> results;
    private long totalHits;
    private int page;
    private int size;
    private long took;
    private Float maxScore;
    private Facets facets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchHit {
        private String productId;
        private String name;
        private String description;
        private String shortDescription;
        private String slug;
        private String sku;
        private Double price;
        private Double originalPrice;
        private Double discountPercent;
        private String imageUrl;
        private String categoryId;
        private String categoryName;
        private Boolean inStock;
        private Double averageRating;
        private Integer reviewCount;
        private List<String> tags;
        private Float score;
        private Map<String, List<String>> highlight;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Facets {
        private List<FacetBucket> categories;
        private List<RangeBucket> priceRanges;
        private List<FacetBucket> tags;
        private List<RangeBucket> ratings;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacetBucket {
        private String key;
        private String label;
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RangeBucket {
        private String key;
        private String label;
        private long count;
        private Double from;
        private Double to;
    }
}
