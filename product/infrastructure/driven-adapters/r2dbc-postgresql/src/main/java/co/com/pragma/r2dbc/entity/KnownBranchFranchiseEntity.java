package co.com.pragma.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad para branch_db.known_branch_franchise (tabla de control sincronizada desde RabbitMQ).
 * No se usa con ReactiveCrudRepository; el adapter usa DatabaseClient con SQL explícito.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KnownBranchFranchiseEntity {

    private UUID branchId;
    private String branchName;
    private UUID franchiseId;
    private Instant occurredOn;
}
