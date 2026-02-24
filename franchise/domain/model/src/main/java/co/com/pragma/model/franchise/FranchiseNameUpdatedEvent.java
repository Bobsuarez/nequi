package co.com.pragma.model.franchise;

import java.time.Instant;
import java.util.UUID;

public record FranchiseNameUpdatedEvent(
        UUID franchiseId,
        String previousName,
        String newName,
        Instant occurredOn
) {
}
