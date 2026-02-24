package co.com.pragma.model.franchise;

import java.util.Objects;
import java.util.UUID;

public record FranchiseId(UUID value) {

    public FranchiseId {
        Objects.requireNonNull(value, "FranchiseId value must not be null");
    }

    public static FranchiseId generate() {
        return new FranchiseId(UUID.randomUUID());
    }

    public static FranchiseId of(UUID value) {
        return new FranchiseId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
