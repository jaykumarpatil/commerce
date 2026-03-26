package se.magnus.microservices.core.catalog.elasticsearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import se.magnus.microservices.core.catalog.persistence.ProductEntity;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductElasticsearchService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductElasticsearchService.class);
    private static final String INDEX_NAME = "products";

    private final ElasticsearchOperations elasticsearchTemplate;

    @Value("${elasticsearch.index.replicas:1}")
    private int numberOfReplicas;

    @Value("${elasticsearch.index.shards:1}")
    private int numberOfShards;

    public ProductElasticsearchService(ElasticsearchOperations elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    public Mono<Void> initializeIndex() {
        return Mono.fromRunnable(() -> {
            IndexCoordinates indexCoordinates = IndexCoordinates.of(INDEX_NAME);
            var indexOps = elasticsearchTemplate.indexOps(indexCoordinates);
            
            if (!indexOps.exists()) {
                indexOps.create();
                LOG.info("Created Elasticsearch index: {}", INDEX_NAME);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<String> indexProduct(ProductEntity product) {
        return Mono.fromCallable(() -> {
            Map<String, Object> document = convertToDocument(product);
            
            IndexQuery indexQuery = new IndexQueryBuilder()
                    .withId(product.getProductId())
                    .withObject(document)
                    .build();
            
            String documentId = elasticsearchTemplate.index(indexQuery, IndexCoordinates.of(INDEX_NAME));
            LOG.info("Indexed product {} with id {}", product.getProductId(), documentId);
            return documentId;
        }).subscribeOn(Schedulers.boundedElastic())
          .onErrorResume(e -> {
              LOG.error("Failed to index product {}: {}", product.getProductId(), e.getMessage());
              return Mono.empty();
          });
    }

    public Mono<Void> indexProducts(List<ProductEntity> products) {
        return Flux.fromIterable(products)
                .flatMap(this::indexProduct)
                .then();
    }

    public Mono<Void> deleteProduct(String productId) {
        return Mono.fromRunnable(() -> {
            elasticsearchTemplate.delete(productId, IndexCoordinates.of(INDEX_NAME));
            LOG.info("Deleted product {} from index", productId);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<Long> searchProducts(String query, List<String> categoryIds, 
                                    Double minPrice, Double maxPrice,
                                    List<String> attributes, int page, int size) {
        return Mono.fromCallable(() -> {
            return 0L;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<String>> getSuggestions(String prefix, int limit) {
        return Mono.fromCallable(() -> {
            return java.util.List.<String>of();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Map<String, Object> convertToDocument(ProductEntity product) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("productId", product.getProductId());
        doc.put("name", product.getName());
        doc.put("description", product.getDescription());
        doc.put("shortDescription", product.getShortDescription());
        doc.put("slug", product.getSlug());
        doc.put("sku", product.getSku());
        doc.put("price", product.getPrice());
        doc.put("originalPrice", product.getOriginalPrice());
        doc.put("discountPercent", product.getDiscountPercent());
        doc.put("imageUrl", product.getImageUrl());
        doc.put("mainImage", product.getMainImage());
        doc.put("images", product.getImages());
        doc.put("stockQuantity", product.getStockQuantity());
        doc.put("inStock", product.isInStock());
        doc.put("featured", product.isFeatured());
        doc.put("active", product.isActive());
        doc.put("categoryId", product.getCategoryId());
        doc.put("attributes", product.getAttributes());
        doc.put("specifications", product.getSpecifications());
        doc.put("tags", product.getTags());
        doc.put("weight", product.getWeight());
        doc.put("averageRating", product.getAverageRating());
        doc.put("reviewCount", product.getReviewCount());
        doc.put("viewCount", product.getViewCount());
        doc.put("orderCount", product.getOrderCount());
        doc.put("createdAt", product.getCreatedAt());
        doc.put("updatedAt", product.getUpdatedAt());
        return doc;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProductDocument {
        private String productId;
        private String name;
        private String description;
        private Double price;
        private String imageUrl;
        private String categoryId;
        private Boolean inStock;
        private List<String> tags;
    }

    private static class Flux {
        static <T> reactor.core.publisher.Flux<T> fromIterable(Iterable<T> iterable) {
            return reactor.core.publisher.Flux.fromIterable(iterable);
        }
    }
}
