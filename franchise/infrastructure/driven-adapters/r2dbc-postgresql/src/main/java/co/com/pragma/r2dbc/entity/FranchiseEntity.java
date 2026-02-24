package co.com.pragma.r2dbc.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("franchises")
@Getter
@Setter
@NoArgsConstructor
public class FranchiseEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    private String name;

    @Transient
    private String namePrevious;

    @Transient
    private boolean isNew;

    @Override
    public boolean isNew() {
        return isNew;
    }
}
