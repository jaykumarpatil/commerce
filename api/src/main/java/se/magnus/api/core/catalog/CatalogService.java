package se.magnus.api.core.catalog;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CatalogService {

    @PostMapping("/v1/categories")
    Mono<Category> createCategory(Category category);

    @GetMapping("/v1/categories")
    Flux<Category> getAllCategories();

    @GetMapping("/v1/categories/{categoryId}")
    Mono<Category> getCategory(String categoryId);

    @PutMapping("/v1/categories/{categoryId}")
    Mono<Category> updateCategory(String categoryId, Category category);

    @DeleteMapping("/v1/categories/{categoryId}")
    Mono<Void> deleteCategory(String categoryId);

    @PostMapping("/v1/products")
    Mono<Product> createProduct(Product product);

    @GetMapping("/v1/products")
    Flux<Product> getAllProducts();

    @GetMapping("/v1/products/{productId}")
    Mono<Product> getProduct(String productId);

    @PutMapping("/v1/products/{productId}")
    Mono<Product> updateProduct(String productId, Product product);

    @DeleteMapping("/v1/products/{productId}")
    Mono<Void> deleteProduct(String productId);

    @GetMapping("/v1/products/search")
    Flux<Product> searchProducts(@RequestParam String query, 
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size);

    @GetMapping("/v1/products/category/{categoryId}")
    Flux<Product> getProductsByCategory(String categoryId);

    @PostMapping("/v1/products/{productId}/variants")
    Mono<Variant> addVariant(String productId, Variant variant);

    @PutMapping("/v1/products/{productId}/variants/{variantId}")
    Mono<Variant> updateVariant(String productId, String variantId, Variant variant);

    @DeleteMapping("/v1/products/{productId}/variants/{variantId}")
    Mono<Void> deleteVariant(String productId, String variantId);
}
