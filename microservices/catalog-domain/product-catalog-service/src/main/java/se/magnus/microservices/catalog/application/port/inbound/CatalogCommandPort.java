package se.magnus.microservices.catalog.application.port.inbound;

import se.magnus.api.core.product.Product;
import reactor.core.publisher.Mono;

public interface CatalogCommandPort {
    Mono<Product> createProduct(Product product);
    Mono<Product> updateProduct(String productId, Product product);
    Mono<Void> deleteProduct(String productId);
}
