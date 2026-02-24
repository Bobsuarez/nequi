package co.com.pragma.validator.dto.response;

import java.util.UUID;

public record BranchResponse(UUID id, UUID franchiseId, String name) {
}
