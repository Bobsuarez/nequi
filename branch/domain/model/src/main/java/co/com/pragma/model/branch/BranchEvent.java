package co.com.pragma.model.branch;

import java.time.Instant;
import java.util.UUID;

public record BranchEvent(
        UUID branchId,
        UUID franchiseId,
        String name,
        Instant occurredOn
) {

    public static BranchEvent of(Branch branch) {
        return new BranchEvent(
                branch.getId().value(),
                branch.getFranchiseId(),
                branch.getName(),
                Instant.now()
        );
    }
}
