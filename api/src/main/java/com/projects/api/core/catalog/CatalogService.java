package com.projects.api.core.catalog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CatalogService {

    @GetMapping("/v1/catalog/categories")
    Flux<Category> getAllCategories();

    @GetMapping("/v1/catalog/categories/{categoryId}")
    Mono<Category> getCategory(@PathVariable String categoryId);

    @GetMapping("/v1/catalog/products")
    Flux<Product> getAllProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size);

    @GetMapping("/v1/catalog/products/{productId}")
    Mono<Product> getProduct(@PathVariable String productId);

    @GetMapping("/v1/catalog/products/search")
    Flux<Product> searchProducts(@RequestParam String query,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "20") int size);

    @GetMapping("/v1/catalog/categories/{categoryId}/products")
    Flux<Product> getProductsByCategory(@PathVariable String categoryId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size);
}
