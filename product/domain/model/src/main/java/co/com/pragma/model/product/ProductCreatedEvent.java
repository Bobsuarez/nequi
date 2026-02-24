package co.com.pragma.model.product;

import java.util.UUID;

public record ProductCreatedEvent(
        UUID productId,
        UUID branchId,
        String name,
        int stock
) {
    public static ProductCreatedEvent of(Product product) {
        return new ProductCreatedEvent(
                product.getId().value(),
                product.getBranchId().value(),
                product.getName(),
                product.getStock()
        );
    }
}
