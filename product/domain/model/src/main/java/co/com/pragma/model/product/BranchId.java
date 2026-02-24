package co.com.pragma.model.product;

import java.util.UUID;

public record BranchId(UUID value) {

    public static BranchId of(UUID value) {
        return value != null ? new BranchId(value) : null;
    }
}
