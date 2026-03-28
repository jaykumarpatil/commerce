package com.projects.microservices.core.catalog.controller;

import com.projects.api.core.catalog.CatalogService;
import com.projects.api.core.catalog.Category;
import com.projects.api.core.catalog.Product;
import com.projects.util.http.ServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class CatalogController implements CatalogService {

    private final CatalogService catalogService;
    @SuppressWarnings("unused")
    private final ServiceUtil serviceUtil;

    @Autowired
    public CatalogController(CatalogService catalogService, ServiceUtil serviceUtil) {
        this.catalogService = catalogService;
        this.serviceUtil = serviceUtil;
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
    public Flux<Product> getAllProducts(int page, int size) {
        return catalogService.getAllProducts(page, size);
    }

    @Override
    public Mono<Product> getProduct(String productId) {
        return catalogService.getProduct(productId);
    }

    @Override
    public Flux<Product> searchProducts(String query, int page, int size) {
        return catalogService.searchProducts(query, page, size);
    }

    @Override
    public Flux<Product> getProductsByCategory(String categoryId, int page, int size) {
        return catalogService.getProductsByCategory(categoryId, page, size);
    }
}
