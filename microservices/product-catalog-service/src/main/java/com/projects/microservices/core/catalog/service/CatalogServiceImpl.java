package com.projects.microservices.core.catalog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.projects.api.core.catalog.*;
import com.projects.api.exceptions.InvalidInputException;
import com.projects.api.exceptions.NotFoundException;
import com.projects.microservices.core.catalog.elasticsearch.ProductElasticsearchService;
import com.projects.microservices.core.catalog.persistence.*;
import com.projects.microservices.core.catalog.service.s3.S3PresignedUrlService;

import java.util.*;

@Service
public class CatalogServiceImpl implements CatalogService {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final VariantMapper variantMapper;
    private final ProductElasticsearchService elasticsearchService;
    private final S3PresignedUrlService s3Service;

    @Value("${elasticsearch.sync.enabled:true}")
    private boolean elasticsearchSyncEnabled;

    @Autowired
    public CatalogServiceImpl(CategoryRepository categoryRepository,
                              ProductRepository productRepository,
                              VariantRepository variantRepository,
                              CategoryMapper categoryMapper,
                              ProductMapper productMapper,
                              VariantMapper variantMapper,
                              ProductElasticsearchService elasticsearchService,
                              S3PresignedUrlService s3Service) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
        this.variantMapper = variantMapper;
        this.elasticsearchService = elasticsearchService;
        this.s3Service = s3Service;
    }

    @Override
    public Mono<Category> createCategory(Category category) {
        if (category.getName() == null || category.getName().isEmpty()) {
            return Mono.error(new InvalidInputException("Category name is required"));
        }
        CategoryEntity entity = categoryMapper.apiToEntity(category);
        String now = java.time.ZonedDateTime.now().toString();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return categoryRepository.save(entity)
                .map(categoryMapper::entityToApi);
    }

    @Override
    public Flux<Category> getAllCategories() {
        return categoryRepository.findAllByActiveTrueOrderBySortOrder()
                .map(categoryMapper::entityToApi);
    }

    @Override
    public Mono<Category> getCategory(String categoryId) {
        return categoryRepository.findByCategoryId(categoryId)
                .switchIfEmpty(Mono.error(new NotFoundException("Category not found: " + categoryId)))
                .map(categoryMapper::entityToApi);
    }

    @Override
    public Mono<Category> updateCategory(String categoryId, Category category) {
        return categoryRepository.findByCategoryId(categoryId)
                .switchIfEmpty(Mono.error(new NotFoundException("Category not found: " + categoryId)))
                .flatMap(entity -> {
                    if (category.getName() != null) entity.setName(category.getName());
                    if (category.getDescription() != null) entity.setDescription(category.getDescription());
                    if (category.getSlug() != null) entity.setSlug(category.getSlug());
                    if (category.getImageUrl() != null) entity.setImageUrl(category.getImageUrl());
                    if (category.getSortOrder() != null) entity.setSortOrder(category.getSortOrder());
                    entity.setActive(category.isActive());
                    entity.setUpdatedAt(java.time.ZonedDateTime.now().toString());
                    return categoryRepository.save(entity).map(categoryMapper::entityToApi);
                });
    }

    @Override
    public Mono<Void> deleteCategory(String categoryId) {
        return categoryRepository.findByCategoryId(categoryId)
                .switchIfEmpty(Mono.error(new NotFoundException("Category not found: " + categoryId)))
                .flatMap(entity -> {
                    entity.setActive(false);
                    entity.setUpdatedAt(java.time.ZonedDateTime.now().toString());
                    return categoryRepository.save(entity);
                }).then();
    }

    @Override
    public Mono<Product> createProduct(Product product) {
        return validateProduct(product)
                .flatMap(p -> categoryRepository.findByCategoryId(p.getCategoryId())
                        .switchIfEmpty(Mono.error(new NotFoundException("Category not found: " + p.getCategoryId())))
                        .then(productRepository.save(productMapper.apiToEntity(p))))
                .doOnSuccess(entity -> syncToElasticsearch(entity).subscribe())
                .map(productMapper::entityToApi);
    }

    @Override
    public Flux<Product> getAllProducts() {
        return productRepository.findAllByActiveTrue()
                .map(productMapper::entityToApi);
    }

    @Override
    public Mono<Product> getProduct(String productId) {
        return productRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product not found: " + productId)))
                .doOnSuccess(entity -> {
                    if (entity != null) {
                        entity.setViewCount(entity.getViewCount() + 1);
                        productRepository.save(entity).subscribe();
                    }
                })
                .map(productMapper::entityToApi);
    }

    @Override
    public Mono<Product> updateProduct(String productId, Product product) {
        return productRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product not found: " + productId)))
                .flatMap(entity -> updateProductEntity(entity, product))
                .doOnSuccess(entity -> syncToElasticsearch(entity).subscribe())
                .map(productMapper::entityToApi);
    }

    @Override
    public Mono<Void> deleteProduct(String productId) {
        return productRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product not found: " + productId)))
                .flatMap(entity -> {
                    entity.setActive(false);
                    entity.setUpdatedAt(java.time.ZonedDateTime.now().toString());
                    return productRepository.save(entity);
                })
                .doOnSuccess(entity -> elasticsearchService.deleteProduct(productId).subscribe())
                .then();
    }

    @Override
    public Flux<Product> searchProducts(String query, int page, int size) {
        if (query == null || query.isBlank()) {
            return Flux.empty();
        }
        
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(query)
                .skip((long) page * size)
                .take(size)
                .map(productMapper::entityToApi);
    }

    @Override
    public Flux<Product> getProductsByCategory(String categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .filter(ProductEntity::isActive)
                .map(productMapper::entityToApi);
    }

    @Override
    public Mono<Variant> addVariant(String productId, Variant variant) {
        return productRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product not found: " + productId)))
                .flatMap(product -> {
                    VariantEntity entity = variantMapper.apiToEntity(variant);
                    entity.setProductId(productId);
                    entity.setCreatedAt(java.time.ZonedDateTime.now().toString());
                    entity.setUpdatedAt(java.time.ZonedDateTime.now().toString());
                    return variantRepository.save(entity);
                })
                .map(variantMapper::entityToApi);
    }

    @Override
    public Mono<Variant> updateVariant(String productId, String variantId, Variant variant) {
        return variantRepository.findByVariantId(variantId)
                .switchIfEmpty(Mono.error(new NotFoundException("Variant not found: " + variantId)))
                .flatMap(entity -> {
                    if (variant.getName() != null) entity.setName(variant.getName());
                    if (variant.getPrice() != null) entity.setPrice(variant.getPrice());
                    if (variant.getStockQuantity() != null) entity.setStockQuantity(variant.getStockQuantity());
                    entity.setActive(variant.isActive());
                    entity.setUpdatedAt(java.time.ZonedDateTime.now().toString());
                    return variantRepository.save(entity);
                })
                .map(variantMapper::entityToApi);
    }

    @Override
    public Mono<Void> deleteVariant(String productId, String variantId) {
        return variantRepository.findByVariantId(variantId)
                .switchIfEmpty(Mono.error(new NotFoundException("Variant not found: " + variantId)))
                .flatMap(entity -> {
                    entity.setActive(false);
                    entity.setUpdatedAt(java.time.ZonedDateTime.now().toString());
                    return variantRepository.save(entity);
                }).then();
    }

    private Mono<Product> validateProduct(Product product) {
        if (product.getName() == null || product.getName().isEmpty()) {
            return Mono.error(new InvalidInputException("Product name is required"));
        }
        if (product.getCategoryId() == null || product.getCategoryId().isEmpty()) {
            return Mono.error(new InvalidInputException("Category ID is required"));
        }
        if (product.getPrice() == null || product.getPrice() < 0) {
            return Mono.error(new InvalidInputException("Valid price is required"));
        }
        return Mono.just(product);
    }

    private Mono<ProductEntity> updateProductEntity(ProductEntity entity, Product product) {
        if (product.getName() != null) entity.setName(product.getName());
        if (product.getDescription() != null) entity.setDescription(product.getDescription());
        if (product.getShortDescription() != null) entity.setShortDescription(product.getShortDescription());
        if (product.getPrice() != null) entity.setPrice(product.getPrice());
        if (product.getCategoryId() != null) entity.setCategoryId(product.getCategoryId());
        if (product.getImageUrl() != null) entity.setImageUrl(product.getImageUrl());
        if (product.getImages() != null) entity.setImages(product.getImages());
        if (product.getAttributes() != null) entity.setAttributes(product.getAttributes());
        entity.setActive(product.isActive());
        entity.setFeatured(product.isFeatured());
        entity.setInStock(product.isInStock());
        if (product.getStockQuantity() != null) entity.setStockQuantity(product.getStockQuantity());
        entity.setUpdatedAt(java.time.ZonedDateTime.now().toString());
        return productRepository.save(entity);
    }

    private Mono<Void> syncToElasticsearch(ProductEntity entity) {
        if (elasticsearchSyncEnabled) {
            return elasticsearchService.indexProduct(entity).then();
        }
        return Mono.empty();
    }
}
