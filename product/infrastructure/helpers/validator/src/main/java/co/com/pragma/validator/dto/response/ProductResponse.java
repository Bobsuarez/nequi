package co.com.pragma.validator.dto.response;

import java.util.UUID;

public record ProductResponse(
        UUID id,
        UUID branchId,
        String name,
        Integer stock
) {}
