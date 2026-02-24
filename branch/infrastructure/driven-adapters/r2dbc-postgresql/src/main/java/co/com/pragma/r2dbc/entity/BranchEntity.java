package co.com.pragma.r2dbc.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("branches")
@Getter
@Setter
@NoArgsConstructor
public class BranchEntity implements Persistable<UUID> {

    @Id
    private UUID id;
    private UUID franchiseId;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    private String namePrevious;

    @Transient
    private boolean isNew;

    @Override
    public boolean isNew() {
        return isNew;
    }
}
