package co.com.pragma.validator.dto.response;

import java.time.Instant;
import java.util.UUID;

public record TopProductByBranchResponse(
        UUID franchiseId,
        UUID branchId,
        UUID productId,
        String productName,
        Integer stock,
        Instant updatedAt
) {}
