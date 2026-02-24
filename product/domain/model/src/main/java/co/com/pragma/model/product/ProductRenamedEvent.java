package co.com.pragma.model.product;

import java.util.UUID;

public record ProductRenamedEvent(UUID productId, String name) {

    public static ProductRenamedEvent of(Product product) {
        return new ProductRenamedEvent(product.getId().value(), product.getName());
    }
}
