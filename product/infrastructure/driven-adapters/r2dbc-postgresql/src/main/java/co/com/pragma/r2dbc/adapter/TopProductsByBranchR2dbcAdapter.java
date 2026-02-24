package co.com.pragma.r2dbc.adapter;

import co.com.pragma.model.product.TopProductByBranch;
import co.com.pragma.model.product.gateways.TopProductsByBranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Sincroniza en product_db.top_products_by_branch (UPSERT por branch_id).
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TopProductsByBranchR2dbcAdapter implements TopProductsByBranchRepository {

    private static final String TABLE = "top_products_by_branch";

    private final DatabaseClient databaseClient;

    @Override
    public Flux<TopProductByBranch> findByFranchiseId(UUID franchiseId) {
        String sql = "SELECT franchise, branch_id, product_id, product_name, stock, updated_at FROM " + TABLE + " WHERE franchise = :franchiseId ORDER BY branch_id";
        return databaseClient.sql(sql)
                .bind("franchiseId", franchiseId)
                .map((row, meta) -> {
                    LocalDateTime updatedAt = row.get("updated_at", LocalDateTime.class);
                    return TopProductByBranch.builder()
                            .franchiseId(row.get("franchise", UUID.class))
                            .branchId(row.get("branch_id", UUID.class))
                            .productId(row.get("product_id", UUID.class))
                            .productName(row.get("product_name", String.class))
                            .stock(row.get("stock", Integer.class) != null ? row.get("stock", Integer.class) : 0)
                            .updatedAt(updatedAt != null ? updatedAt.toInstant(ZoneOffset.UTC) : null)
                            .build();
                })
                .all();
    }

    @Override
    public Mono<Void> saveOrUpdate(TopProductByBranch top) {
        LocalDateTime updatedAt = top.getUpdatedAt() != null
                ? LocalDateTime.ofInstant(top.getUpdatedAt(), ZoneOffset.UTC)
                : LocalDateTime.now();
        String sql = "INSERT INTO " + TABLE + " (franchise, branch_id, product_id, product_name, stock, updated_at) "
                + "VALUES (:franchise, :branchId, :productId, :productName, :stock, :updatedAt) ";
        return databaseClient.sql(sql)
                .bind("franchise", top.getFranchiseId())
                .bind("branchId", top.getBranchId())
                .bind("productId", top.getProductId())
                .bind("productName", top.getProductName())
                .bind("stock", top.getStock())
                .bind("updatedAt", updatedAt)
                .fetch()
                .rowsUpdated()
                .then();
    }

    @Override
    public Mono<Void> deleteByBranchId(UUID productId) {
            String sql = "DELETE FROM " + TABLE + " WHERE product_id = :productId";
            return databaseClient.sql(sql)
                    .bind("productId", productId)
                    .fetch()
                    .rowsUpdated()
                    .then();
    }
}
