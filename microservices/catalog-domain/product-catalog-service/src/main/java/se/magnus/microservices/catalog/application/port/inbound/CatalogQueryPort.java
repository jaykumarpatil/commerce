package se.magnus.microservices.catalog.application.port.inbound;

import se.magnus.api.core.product.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CatalogQueryPort {
    Mono<Product> getProduct(String productId);
    Flux<Product> getProductsByCategory(String categoryId);
    Flux<Product> searchProducts(String query);
}
