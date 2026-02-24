package co.com.pragma.model.product;

import java.util.UUID;

public record ProductDeletedEvent(UUID productId) {

    public static ProductDeletedEvent of(Product product) {
        return new ProductDeletedEvent(product.getId().value());
    }
}
