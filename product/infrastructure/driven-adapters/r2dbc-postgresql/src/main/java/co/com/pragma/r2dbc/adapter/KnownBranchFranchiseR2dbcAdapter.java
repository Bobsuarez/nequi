package co.com.pragma.r2dbc.adapter;

import co.com.pragma.model.product.KnownBranchFranchise;
import co.com.pragma.model.product.gateways.KnownBranchFranchiseRepository;
import co.com.pragma.r2dbc.mappers.KnownBranchFranchiseEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Adapter que persiste en branch_db.known_branch_franchise con UPSERT idempotente.
 * Si branch_id ya existe, se actualiza. No falla.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class KnownBranchFranchiseR2dbcAdapter implements KnownBranchFranchiseRepository {

    private static final String TABLE = "product_db.known_branch_franchise";

    private final DatabaseClient databaseClient;
    private final KnownBranchFranchiseEntityMapper mapper;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<Boolean> existsByBranchId(java.util.UUID branchId) {
        String sql = "SELECT 1 FROM " + TABLE + " WHERE branch_id = :branchId LIMIT 1";
        return databaseClient.sql(sql)
                .bind("branchId", branchId)
                .map((row, meta) -> 1)
                .one()
                .map(i -> true)
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<KnownBranchFranchise> findByBranchId(java.util.UUID branchId) {
        String sql = "SELECT branch_id, branch_name, franchise_id, occurredon FROM " + TABLE + " WHERE branch_id = " +
                ":branchId LIMIT 1";
        return databaseClient.sql(sql)
                .bind("branchId", branchId)
                .map((row, meta) -> {
                    LocalDateTime occurredOn = row.get("occurredOn", LocalDateTime.class);
                    return KnownBranchFranchise.of(
                            row.get("branch_id", java.util.UUID.class),
                            row.get("branch_name", String.class),
                            row.get("franchise_id", java.util.UUID.class),
                            occurredOn != null ? occurredOn.toInstant(ZoneOffset.UTC) : null);
                })
                .one();
    }

    @Override
    public Mono<KnownBranchFranchise> saveOrUpdate(KnownBranchFranchise knownBranchFranchise) {
        String sql = "INSERT INTO " + TABLE + " (branch_id, branch_name, franchise_id, occurredon) "
                + "VALUES (:branchId, :branchName, :franchiseId, :occurredOn) "
                + "ON CONFLICT (branch_id) DO UPDATE SET "
                + "branch_name = EXCLUDED.branch_name, "
                + "franchise_id = EXCLUDED.franchise_id, "
                + "occurredon = EXCLUDED.occurredon";
        java.util.UUID branchId = knownBranchFranchise.getBranchId().value();
        Instant occurredOn = knownBranchFranchise.getOccurredOn() != null
                ? knownBranchFranchise.getOccurredOn()
                : Instant.now();
        Mono<KnownBranchFranchise> save = databaseClient.sql(sql)
                .bind("branchId", branchId)
                .bind("branchName", knownBranchFranchise.getBranchName() != null ? knownBranchFranchise.getBranchName() : "")
                .bind("franchiseId", knownBranchFranchise.getFranchiseId())
                .bind("occurredOn", occurredOn)
                .fetch()
                .rowsUpdated()
                .thenReturn(knownBranchFranchise);
        return transactionalOperator.transactional(save);
    }
}
