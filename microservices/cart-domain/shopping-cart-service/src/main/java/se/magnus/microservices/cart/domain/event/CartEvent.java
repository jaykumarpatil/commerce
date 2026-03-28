package se.magnus.microservices.cart.domain.event;

import java.time.Instant;
import java.util.List;

public sealed interface CartEvent permits 
    CartEvent.ItemAdded, 
    CartEvent.ItemRemoved, 
    CartEvent.ItemUpdated,
    CartEvent.CartCleared,
    CartEvent.CartMerged {

    record ItemAdded(
        String cartId,
        String cartItemId,
        String productId,
        int quantity,
        Instant timestamp
    ) implements CartEvent {}

    record ItemRemoved(
        String cartId,
        String cartItemId,
        String productId,
        Instant timestamp
    ) implements CartEvent {}

    record ItemUpdated(
        String cartId,
        String cartItemId,
        String productId,
        int oldQuantity,
        int newQuantity,
        Instant timestamp
    ) implements CartEvent {}

    record CartCleared(
        String cartId,
        Instant timestamp
    ) implements CartEvent {}

    record CartMerged(
        String targetCartId,
        String sourceCartId,
        List<String> mergedItemIds,
        Instant timestamp
    ) implements CartEvent {}
}
