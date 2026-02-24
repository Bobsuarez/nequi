package co.com.pragma.model.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Modelo de dominio para la relación branch–franchise sincronizada desde RabbitMQ.
 * Se persiste en branch_db.known_branch_franchise.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnownBranchFranchise {

    private BranchId branchId;
    private String branchName;
    private java.util.UUID franchiseId;
    private Instant occurredOn;

    public static KnownBranchFranchise of(java.util.UUID branchId, String branchName,
                                          java.util.UUID franchiseId, Instant occurredOn) {
        return KnownBranchFranchise.builder()
                .branchId(BranchId.of(branchId))
                .branchName(branchName != null ? branchName.trim() : null)
                .franchiseId(franchiseId)
                .occurredOn(occurredOn != null ? occurredOn : Instant.now())
                .build();
    }
}
