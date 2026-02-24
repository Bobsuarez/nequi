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
public class KnownFranchise {

    private UUID id;
    private String name;

    public static KnownFranchise of(UUID id, String name) {
        return KnownFranchise.builder()
                .id(id)
                .name(name != null ? name.trim() : null)
                .build();
    }
}
