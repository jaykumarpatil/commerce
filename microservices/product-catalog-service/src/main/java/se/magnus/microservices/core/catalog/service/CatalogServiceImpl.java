package se.magnus.microservices.core.catalog.service;

import static java.util.logging.Level.FINE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.catalog.*;
import se.magnus.api.exceptions.BadRequestException;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.microservices.core.catalog.persistence.*;

@Service
public class CatalogServiceImpl implements CatalogService {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final VariantMapper variantMapper;

    @Autowired
    public CatalogServiceImpl(CategoryRepository categoryRepository, 
                              ProductRepository productRepository,
                              VariantRepository variantRepository,
                              CategoryMapper categoryMapper,
                              ProductMapper productMapper,
                              VariantMapper variantMapper) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
        this.variantMapper = variantMapper;
    }

    @Override
    public Mono<Category> createCategory(Category category) {
        if (category.getName() == null || category.getName().isEmpty()) {
            return Mono.error(new InvalidInputException("Category name is required"));
        }

        CategoryEntity entity = categoryMapper.apiToEntity(category);
        
        return categoryRepository.save(entity)
                .log(LOG.getName(), FINE)
                .onErrorMap(DataIntegrityViolationException.class, 
                        ex -> new InvalidInputException("Duplicate category: " + category.getName()))
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

                    return categoryRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(categoryMapper::entityToApi);
                });
    }

    @Override
    public Mono<Void> deleteCategory(String categoryId) {
        return categoryRepository.findByCategoryId(categoryId)
                .switchIfEmpty(Mono.error(new NotFoundException("Category not found: " + categoryId)))
                .flatMap(categoryRepository::delete)
                .then(Mono.empty());
    }

    @Override
    public Mono<Product> createProduct(Product product) {
        if (product.getName() == null || product.getName().isEmpty()) {
            return Mono.error(new InvalidInputException("Product name is required"));
        }
        if (product.getCategoryId() == null || product.getCategoryId().isEmpty()) {
            return Mono.error(new InvalidInputException("Category ID is required"));
        }

        ProductEntity entity = productMapper.apiToEntity(product);
        
        return categoryRepository.findByCategoryId(product.getCategoryId())
                .switchIfEmpty(Mono.error(new NotFoundException("Category not found: " + product.getCategoryId())))
                .flatMap(cat -> productRepository.save(entity))
                .log(LOG.getName(), FINE)
                .onErrorMap(DataIntegrityViolationException.class, 
                        ex -> new InvalidInputException("Duplicate product SKU: " + product.getSku()))
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
                .map(productMapper::entityToApi);
    }

    @Override
    public Mono<Product> updateProduct(String productId, Product product) {
        return productRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product not found: " + productId)))
                .flatMap(entity -> {
                    if (product.getName() != null) entity.setName(product.getName());
                    if (product.getDescription() != null) entity.setDescription(product.getDescription());
                    if (product.getShortDescription() != null) entity.setShortDescription(product.getShortDescription());
                    if (product.getPrice() != null) entity.setPrice(product.getPrice());
                    if (product.getCategoryId() != null) entity.setCategoryId(product.getCategoryId());
                    entity.setActive(product.isActive());

                    return productRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(productMapper::entityToApi);
                });
    }

    @Override
    public Mono<Void> deleteProduct(String productId) {
        return productRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product not found: " + productId)))
                .flatMap(productRepository::delete)
                .then(Mono.empty());
    }

    @Override
    public Flux<Product> searchProducts(String query, int page, int size) {
        if (query == null || query.isEmpty()) {
            return Flux.empty();
        }
        
        LOG.info("Searching products with query: {}, page: {}, size: {}", query, page, size);
        
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(query)
                .skip(page * size)
                .take(size)
                .map(productMapper::entityToApi);
    }

    @Override
    public Flux<Product> getProductsByCategory(String categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .map(productMapper::entityToApi);
    }

    @Override
    public Mono<Variant> addVariant(String productId, Variant variant) {
        return productRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("Product not found: " + productId)))
                .flatMap(product -> {
                    VariantEntity entity = variantMapper.apiToEntity(variant);
                    entity.setProductId(productId);
                    return variantRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(variantMapper::entityToApi);
                });
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

                    return variantRepository.save(entity)
                            .log(LOG.getName(), FINE)
                            .map(variantMapper::entityToApi);
                });
    }

    @Override
    public Mono<Void> deleteVariant(String productId, String variantId) {
        return variantRepository.findByVariantId(variantId)
                .switchIfEmpty(Mono.error(new NotFoundException("Variant not found: " + variantId)))
                .flatMap(variantRepository::delete)
                .then(Mono.empty());
    }
}
