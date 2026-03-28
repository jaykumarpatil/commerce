package com.projects.microservices.core.order.service.adapter.outbound;

import com.projects.api.core.cart.Cart;
import com.projects.microservices.core.order.service.port.outbound.ShoppingCartPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ShoppingCartWebClientAdapter implements ShoppingCartPort {

    private final WebClient webClient;

    public ShoppingCartWebClientAdapter(
            WebClient.Builder builder,
            @Value("${app.services.shopping-cart.base-url:http://shopping-cart-service}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<Cart> getCart(String cartId) {
        return webClient.get()
                .uri("/v1/carts/{cartId}", cartId)
                .retrieve()
                .bodyToMono(Cart.class);
    }

    @Override
    public Mono<Void> clearCart(String cartId) {
        return webClient.delete()
                .uri("/v1/carts/{cartId}", cartId)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
