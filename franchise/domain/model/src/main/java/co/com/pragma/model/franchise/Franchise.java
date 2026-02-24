package co.com.pragma.model.franchise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Franchise {

    private FranchiseId id;
    private String name;

    public static Franchise create(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Franchise name must not be blank");
        }
        return Franchise.builder()
                .id(FranchiseId.generate())
                .name(name.trim())
                .build();
    }

    @Override
    public String toString() {
        return "Franchise{id=" + id + ", name='" + name + "'}";
    }
}
