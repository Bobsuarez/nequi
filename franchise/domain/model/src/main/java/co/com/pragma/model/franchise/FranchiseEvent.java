package co.com.pragma.model.franchise;

import java.time.Instant;
import java.util.UUID;

public record FranchiseEvent(
        UUID franchiseId,
        String name,
        Instant occurredOn
) {

    public static FranchiseEvent of(Franchise franchise) {
        return new FranchiseEvent(
                franchise.getId().value(),
                franchise.getName(),
                Instant.now()
        );
    }
}
