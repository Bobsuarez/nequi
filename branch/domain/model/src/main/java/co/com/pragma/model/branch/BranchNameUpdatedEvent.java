package co.com.pragma.model.branch;

import java.time.Instant;
import java.util.UUID;

public record BranchNameUpdatedEvent(
        UUID branchId,
        String previousName,
        String newName,
        Instant occurredOn
) {
}
