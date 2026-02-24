package co.com.pragma.model.product;

import java.util.UUID;

public record ProductId(UUID value) {

    public static ProductId generate() {
        return new ProductId(UUID.randomUUID());
    }

    public static ProductId of(UUID value) {
        return value != null ? new ProductId(value) : null;
    }
}
