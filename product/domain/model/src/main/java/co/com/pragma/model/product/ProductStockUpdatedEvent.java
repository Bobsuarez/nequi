package co.com.pragma.model.product;

import java.util.UUID;

public record ProductStockUpdatedEvent(UUID productId, int stock) {

    public static ProductStockUpdatedEvent of(Product product) {
        return new ProductStockUpdatedEvent(product.getId().value(), product.getStock());
    }
}
