package se.magnus.microservices.core.search.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import se.magnus.microservices.core.search.dto.SearchRequest;
import se.magnus.microservices.core.search.dto.SearchResponse;
import se.magnus.microservices.core.search.dto.SuggestionResponse;
import se.magnus.microservices.core.search.service.SearchService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<SearchResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "relevance") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        return searchService.searchWithFilters(q, category, minPrice, maxPrice, tags, 
                inStock, sortBy, sortOrder, page, size);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<SearchResponse> advancedSearch(@RequestBody SearchRequest request) {
        return searchService.search(request);
    }

    @GetMapping(value = "/suggest", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<SuggestionResponse>> getSuggestions(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {
        
        return searchService.getSuggestions(q, limit);
    }

    @GetMapping(value = "/autocomplete", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<String>> autocomplete(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit) {
        
        return searchService.getAutocompleteSuggestions(q, limit);
    }

    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<IndexStatsResponse> getIndexStats() {
        return searchService.getIndexCount()
                .map(count -> IndexStatsResponse.builder()
                        .totalProducts(count)
                        .build());
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IndexStatsResponse {
        private long totalProducts;
    }
}
