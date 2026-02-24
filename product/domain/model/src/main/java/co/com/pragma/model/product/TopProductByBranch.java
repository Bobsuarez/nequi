package co.com.pragma.model.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Modelo de dominio para la tabla top_products_by_branch (sincronizada desde outbox).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductByBranch {

    private UUID franchiseId;
    private UUID branchId;
    private UUID productId;
    private String productName;
    private int stock;
    private Instant updatedAt;
}
