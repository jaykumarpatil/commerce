package se.magnus.microservices.catalog.adapter.outbound.persistence;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.microservices.catalog.application.port.outbound.ProductRepositoryPort;
import se.magnus.microservices.catalog.domain.model.ProductEntity;

@Service
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductMongoRepository mongoRepository;

    public ProductRepositoryAdapter(ProductMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public Mono<ProductEntity> save(ProductEntity product) {
        return mongoRepository.save(product);
    }

    @Override
    public Mono<ProductEntity> findByProductId(String productId) {
        return mongoRepository.findByProductId(productId);
    }

    @Override
    public Flux<ProductEntity> findByCategoryId(String categoryId) {
        return mongoRepository.findByCategoryId(categoryId);
    }

    @Override
    public Flux<ProductEntity> searchByName(String name) {
        return mongoRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public Mono<Void> delete(String productId) {
        return findByProductId(productId).flatMap(mongoRepository::delete);
    }
}
