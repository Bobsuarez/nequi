package co.com.pragma.model.branch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Branch {

    private BranchId id;
    private UUID franchiseId;
    private String name;

    public static Branch create(UUID franchiseId, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Branch name must not be blank");
        }
        if (franchiseId == null) {
            throw new IllegalArgumentException("Franchise id must not be null");
        }
        return Branch.builder()
                .id(BranchId.generate())
                .franchiseId(franchiseId)
                .name(name.trim())
                .build();
    }

    @Override
    public String toString() {
        return "Branch{id=" + id + ", franchiseId=" + franchiseId + ", name='" + name + "'}";
    }
}
