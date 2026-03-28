package com.projects.microservices.core.catalog.service;

import com.projects.api.core.catalog.CatalogService;
import com.projects.api.core.catalog.Product;
import com.projects.api.exceptions.NotFoundException;
import com.projects.microservices.core.catalog.outbound.port.InventoryReadPort;
import com.projects.microservices.core.catalog.outbound.port.ProductReadPort;
import com.projects.microservices.core.catalog.outbound.port.RecommendationReadPort;
import com.projects.microservices.core.catalog.outbound.port.ReviewReadPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class CatalogServiceImpl implements CatalogService {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogServiceImpl.class);

    private final ProductReadPort productReadPort;
    private final ReviewReadPort reviewReadPort;
    private final InventoryReadPort inventoryReadPort;
    private final RecommendationReadPort recommendationReadPort;

    @Autowired
    public CatalogServiceImpl(ProductReadPort productReadPort,
                              ReviewReadPort reviewReadPort,
                              InventoryReadPort inventoryReadPort,
                              RecommendationReadPort recommendationReadPort) {
        this.productReadPort = productReadPort;
        this.reviewReadPort = reviewReadPort;
        this.inventoryReadPort = inventoryReadPort;
        this.recommendationReadPort = recommendationReadPort;
    }

    @Override
    public Flux<com.projects.api.core.catalog.Category> getAllCategories() {
        return productReadPort.getAllCategories();
    }

    @Override
    public Mono<com.projects.api.core.catalog.Category> getCategory(String categoryId) {
        return productReadPort.getCategory(categoryId)
                .switchIfEmpty(Mono.error(new NotFoundException("Category not found: " + categoryId)));
    }

    @Override
    public Flux<Product> getAllProducts(int page, int size) {
        return productReadPort.getAllProducts(page, size)
                .flatMap(this::enrichProductForCatalogRead);
    }

    @Override
    public Mono<Product> getProduct(String productId) {
        return productReadPort.getProduct(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product not found: " + productId)))
                .flatMap(this::enrichProductForCatalogRead);
    }

    @Override
    public Flux<Product> searchProducts(String query, int page, int size) {
        if (query == null || query.isBlank()) {
            return Flux.empty();
        }

        return productReadPort.searchProducts(query, page, size)
                .flatMap(this::enrichProductForCatalogRead);
    }

    @Override
    public Flux<Product> getProductsByCategory(String categoryId, int page, int size) {
        return productReadPort.getProductsByCategory(categoryId, page, size)
                .flatMap(this::enrichProductForCatalogRead);
    }

    private Mono<Product> enrichProductForCatalogRead(Product product) {
        Mono<Integer> reviewCountMono = reviewReadPort.getReviewsByProductId(product.getProductId())
                .count()
                .map(Long::intValue)
                .onErrorReturn(0);

        Mono<List<com.projects.api.core.recommendation.Recommendation>> recommendationsMono =
                recommendationReadPort.getRecommendationsByProductId(product.getProductId())
                        .collectList()
                        .onErrorReturn(List.of());

        Mono<com.projects.api.core.inventory.InventoryItem> inventoryMono = inventoryReadPort
                .getInventoryByProductId(product.getProductId())
                .onErrorResume(ex -> {
                    LOG.warn("Inventory enrichment unavailable for productId={}", product.getProductId(), ex);
                    return Mono.empty();
                });

        return Mono.zip(reviewCountMono, recommendationsMono.defaultIfEmpty(List.of()), inventoryMono.defaultIfEmpty(null))
                .map(tuple -> {
                    Integer reviewCount = tuple.getT1();
                    List<com.projects.api.core.recommendation.Recommendation> recommendations = tuple.getT2();
                    com.projects.api.core.inventory.InventoryItem inventory = tuple.getT3();

                    product.setReviewCount(reviewCount);
                    product.setAverageRating(recommendations.isEmpty() ? null :
                            recommendations.stream().mapToInt(com.projects.api.core.recommendation.Recommendation::getRate).average().orElse(0.0));
                    product.setSimilarProductIds(recommendations.stream()
                            .map(rec -> String.valueOf(rec.getRecommendationId()))
                            .toList());

                    if (inventory != null) {
                        product.setStockQuantity(inventory.getAvailableQuantity());
                        product.setInStock(inventory.isInStock());
                        product.setStockStatus(inventory.getStatus());
                    }

                    return product;
                });
    }
}
