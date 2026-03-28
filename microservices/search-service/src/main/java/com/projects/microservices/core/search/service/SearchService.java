package com.projects.microservices.core.search.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import com.projects.microservices.core.search.document.ProductSearchDocument;
import com.projects.microservices.core.search.dto.SearchRequest;
import com.projects.microservices.core.search.dto.SearchResponse;
import com.projects.microservices.core.search.dto.SuggestionResponse;

import java.util.*;

@Service
public class SearchService {

    private static final Logger LOG = LoggerFactory.getLogger(SearchService.class);

    private final ElasticsearchOperations elasticsearchOperations;

    @Value("${search.default-page-size:20}")
    private int defaultPageSize;

    @Value("${search.max-page-size:100}")
    private int maxPageSize;

    @Value("${search.min-score:0.1}")
    private float minScore;

    public SearchService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public Mono<SearchResponse> search(SearchRequest request) {
        return Mono.fromCallable(() -> {
            int page = request.getPage() != null ? request.getPage() : 0;
            int size = Math.min(request.getSize() != null ? request.getSize() : defaultPageSize, maxPageSize);
            
            NativeQuery searchQuery = buildSearchQuery(request, page, size);
            
            SearchHits<ProductSearchDocument> hits = elasticsearchOperations.search(
                    searchQuery, ProductSearchDocument.class);
            
            List<SearchResponse.SearchHit> results = hits.getSearchHits().stream()
                    .map(hit -> SearchResponse.SearchHit.builder()
                            .productId(hit.getContent().getProductId())
                            .name(hit.getContent().getName())
                            .description(hit.getContent().getDescription())
                            .shortDescription(hit.getContent().getShortDescription())
                            .slug(hit.getContent().getSlug())
                            .sku(hit.getContent().getSku())
                            .price(hit.getContent().getPrice())
                            .originalPrice(hit.getContent().getOriginalPrice())
                            .discountPercent(hit.getContent().getDiscountPercent())
                            .imageUrl(hit.getContent().getImageUrl())
                            .categoryId(hit.getContent().getCategoryId())
                            .categoryName(hit.getContent().getCategoryName())
                            .inStock(hit.getContent().getInStock())
                            .averageRating(hit.getContent().getAverageRating())
                            .reviewCount(hit.getContent().getReviewCount())
                            .tags(hit.getContent().getTags())
                            .score(hit.getScore())
                            .highlight(hit.getHighlightFields())
                            .build())
                    .toList();
            
            return SearchResponse.builder()
                    .results(results)
                    .totalHits(hits.getTotalHits())
                    .page(page)
                    .size(size)
                    .took(hits.getTime())
                    .maxScore(hits.getMaxScore())
                    .build();
        }).subscribeOn(Schedulers.boundedElastic())
          .doOnError(e -> LOG.error("Search failed for query '{}': {}", request.getQuery(), e.getMessage()));
    }

    public Mono<List<SuggestionResponse>> getSuggestions(String prefix, int limit) {
        return Mono.fromCallable(() -> {
            if (prefix == null || prefix.length() < 2) {
                return Collections.emptyList();
            }
            
            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> b
                            .should(s -> s.matchPhrasePrefix(mp -> mp
                                    .field("name")
                                    .query(prefix)
                                    .boost(3.0f)))
                            .should(s -> s.matchPhrasePrefix(mp -> mp
                                    .field("description")
                                    .query(prefix)
                                    .boost(1.0f)))
                            .should(s -> s.match(m -> m
                                    .field("tags")
                                    .query(prefix)
                                    .boost(2.0f)))
                            .filter(f -> f.term(t -> t
                                    .field("active")
                                    .value(true)))))
                    .withPageable(PageRequest.of(0, limit))
                    .build();
            
            SearchHits<ProductSearchDocument> hits = elasticsearchOperations.search(
                    query, ProductSearchDocument.class);
            
            return hits.getSearchHits().stream()
                    .map(hit -> SuggestionResponse.builder()
                            .text(hit.getContent().getName())
                            .productId(hit.getContent().getProductId())
                            .categoryId(hit.getContent().getCategoryId())
                            .imageUrl(hit.getContent().getImageUrl())
                            .price(hit.getContent().getPrice())
                            .score(hit.getScore())
                            .build())
                    .toList();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<String>> getAutocompleteSuggestions(String prefix, int limit) {
        return Mono.fromCallable(() -> {
            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> b
                            .should(s -> s.prefix(p -> p
                                    .field("name")
                                    .value(prefix.toLowerCase())
                                    .boost(2.0f)))
                            .filter(f -> f.term(t -> t
                                    .field("active")
                                    .value(true)))))
                    .withPageable(PageRequest.of(0, limit))
                    .build();
            
            SearchHits<ProductSearchDocument> hits = elasticsearchOperations.search(
                    query, ProductSearchDocument.class);
            
            return hits.getSearchHits().stream()
                    .map(hit -> hit.getContent().getName())
                    .distinct()
                    .limit(limit)
                    .toList();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<SearchResponse> searchWithFilters(String query, String categoryId, 
                                                   Double minPrice, Double maxPrice,
                                                   List<String> tags, Boolean inStock,
                                                   String sortBy, String sortOrder,
                                                   int page, int size) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .categoryIds(categoryId != null ? List.of(categoryId) : null)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .tags(tags)
                .inStock(inStock)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .page(page)
                .size(size)
                .build();
        
        return search(request);
    }

    private NativeQuery buildSearchQuery(SearchRequest request, int page, int size) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        
        if (request.getQuery() != null && !request.getQuery().isBlank()) {
            String queryText = request.getQuery().trim();
            
            boolQuery.must(m -> m.bool(b -> b
                    .should(s -> s.multiMatch(mm -> mm
                            .query(queryText)
                            .fields("name^4", "name.search^3", "name.keyword^5",
                                   "description^2", "shortDescription^3",
                                   "tags^2", "sku^3", "categoryName^2")
                            .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                            .fuzziness("AUTO")))
                    .should(s -> s.matchPhrase(mp -> mp
                            .field("name")
                            .query(queryText)
                            .boost(3.0f)))
                    .should(s -> s.matchPhrasePrefix(mpp -> mpp
                            .field("name")
                            .query(queryText)
                            .boost(2.0f)))));
        }
        
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<FieldValue> categoryValues = request.getCategoryIds().stream()
                    .map(FieldValue::of)
                    .toList();
            boolQuery.filter(f -> f.terms(t -> t
                    .field("categoryId")
                    .terms(TermsQueryField.of(tf -> tf.value(categoryValues)))));
        }
        
        if (request.getMinPrice() != null) {
            boolQuery.filter(f -> f.range(r -> r
                    .field("price")
                    .gte(co.elastic.clients.json.JsonData.of(request.getMinPrice()))));
        }
        
        if (request.getMaxPrice() != null) {
            boolQuery.filter(f -> f.range(r -> r
                    .field("price")
                    .lte(co.elastic.clients.json.JsonData.of(request.getMaxPrice()))));
        }
        
        if (request.getInStock() != null && request.getInStock()) {
            boolQuery.filter(f -> f.term(t -> t
                    .field("inStock")
                    .value(true)));
        }
        
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            for (String tag : request.getTags()) {
                boolQuery.filter(f -> f.term(t -> t
                        .field("tags")
                        .value(tag)));
            }
        }
        
        boolQuery.filter(f -> f.term(t -> t
                .field("active")
                .value(true)));
        
        Query finalQuery;
        if (request.getQuery() != null && !request.getQuery().isBlank()) {
            finalQuery = Query.of(q -> q
                    .functionScore(fs -> fs
                            .query(Query.of(qq -> qq.bool(boolQuery.build())))
                            .functions(fn -> fn
                                    .fieldValueFactor(fvf -> fvf
                                            .field("popularityScore")
                                            .factor(1.2)
                                            .modifier(co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier.Log1p)
                                            .missing(1.0)))
                            .scoreMode(FunctionScoreMode.Sum)
                            .boostMode(FunctionBoostMode.Multiply)));
        } else {
            finalQuery = Query.of(q -> q.bool(boolQuery.build()));
        }
        
        NativeQuery.Builder queryBuilder = NativeQuery.builder()
                .withQuery(finalQuery)
                .withPageable(PageRequest.of(page, size));
        
        if (request.getSortBy() != null) {
            SortOrder order = "desc".equalsIgnoreCase(request.getSortOrder()) 
                    ? SortOrder.Desc : SortOrder.Asc;
            String field = switch (request.getSortBy().toLowerCase()) {
                case "price" -> "price";
                case "name" -> "name.keyword";
                case "rating" -> "averageRating";
                case "popularity" -> "popularityScore";
                case "created" -> "createdAt";
                case "relevance" -> "_score";
                default -> "_score";
            };
            queryBuilder.withSort(s -> s.field(f -> f.field(field).order(order)));
        }
        
        return queryBuilder.build();
    }

    public Mono<Void> indexProduct(ProductSearchDocument document) {
        return Mono.fromRunnable(() -> {
            document.setSuggest(new Completion(new String[]{document.getName()}, 1));
            elasticsearchOperations.save(document);
            LOG.info("Indexed product: {}", document.getProductId());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<Void> deleteProduct(String productId) {
        return Mono.fromRunnable(() -> {
            elasticsearchOperations.delete(productId, ProductSearchDocument.class);
            LOG.info("Deleted product from index: {}", productId);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<Long> getIndexCount() {
        return Mono.fromCallable(() -> {
            SearchHits<ProductSearchDocument> hits = elasticsearchOperations.search(
                    NativeQuery.builder()
                            .withQuery(q -> q.matchAll(m -> m))
                            .withPageable(PageRequest.of(0, 1))
                            .build(),
                    ProductSearchDocument.class);
            return hits.getTotalHits();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
