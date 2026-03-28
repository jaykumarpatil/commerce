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
public class SearchRequest {
    private String query;
    private List<String> categoryIds;
    private Double minPrice;
    private Double maxPrice;
    private List<String> tags;
    private Boolean inStock;
    private Boolean featured;
    private List<Map<String, Object>> attributeFilters;
    private String sortBy;
    private String sortOrder;
    private Integer page;
    private Integer size;
}
