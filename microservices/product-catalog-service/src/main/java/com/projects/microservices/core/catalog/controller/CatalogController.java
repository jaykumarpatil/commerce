package com.projects.microservices.core.catalog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.projects.api.core.catalog.*;
import com.projects.util.http.ServiceUtil;

@RestController
public class CatalogController implements CatalogService {

    private final CatalogService catalogService;
    private final ServiceUtil serviceUtil;

    @Autowired
    public CatalogController(CatalogService catalogService, ServiceUtil serviceUtil) {
        this.catalogService = catalogService;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Category> createCategory(Category category) {
        return catalogService.createCategory(category);
    }

    @Override
    public Flux<Category> getAllCategories() {
        return catalogService.getAllCategories();
    }

    @Override
    public Mono<Category> getCategory(String categoryId) {
        return catalogService.getCategory(categoryId);
    }

    @Override
    public Mono<Category> updateCategory(String categoryId, Category category) {
        return catalogService.updateCategory(categoryId, category);
    }

    @Override
    public Mono<Void> deleteCategory(String categoryId) {
        return catalogService.deleteCategory(categoryId);
    }

    @Override
    public Mono<Product> createProduct(Product product) {
        return catalogService.createProduct(product);
    }

    @Override
    public Flux<Product> getAllProducts() {
        return catalogService.getAllProducts();
    }

    @Override
    public Mono<Product> getProduct(String productId) {
        return catalogService.getProduct(productId);
    }

    @Override
    public Mono<Product> updateProduct(String productId, Product product) {
        return catalogService.updateProduct(productId, product);
    }

    @Override
    public Mono<Void> deleteProduct(String productId) {
        return catalogService.deleteProduct(productId);
    }

    @Override
    public Flux<Product> searchProducts(String query, int page, int size) {
        return catalogService.searchProducts(query, page, size);
    }

    @Override
    public Flux<Product> getProductsByCategory(String categoryId) {
        return catalogService.getProductsByCategory(categoryId);
    }

    @Override
    public Mono<Variant> addVariant(String productId, Variant variant) {
        return catalogService.addVariant(productId, variant);
    }

    @Override
    public Mono<Variant> updateVariant(String productId, String variantId, Variant variant) {
        return catalogService.updateVariant(productId, variantId, variant);
    }

    @Override
    public Mono<Void> deleteVariant(String productId, String variantId) {
        return catalogService.deleteVariant(productId, variantId);
    }
}
