package co.com.pragma.model.branch;

import java.util.Objects;
import java.util.UUID;

public record BranchId(UUID value) {

    public BranchId {
        Objects.requireNonNull(value, "BranchId value must not be null");
    }

    public static BranchId generate() {
        return new BranchId(UUID.randomUUID());
    }

    public static BranchId of(UUID value) {
        return new BranchId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
