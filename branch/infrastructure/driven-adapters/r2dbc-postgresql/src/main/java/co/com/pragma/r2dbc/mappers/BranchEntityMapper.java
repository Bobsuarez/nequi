package co.com.pragma.r2dbc.mappers;

import co.com.pragma.model.branch.Branch;
import co.com.pragma.model.branch.BranchId;
import co.com.pragma.r2dbc.entity.BranchEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface BranchEntityMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "namePrevious", ignore = true)
    @Mapping(target = "new", ignore = true)
    BranchEntity toEntity(Branch branch);

    Branch toDomain(BranchEntity entity);

    default UUID map(BranchId branchId) {
        return branchId != null ? branchId.value() : null;
    }

    default BranchId map(UUID value) {
        return value != null ? BranchId.of(value) : null;
    }

    @AfterMapping
    default void markAsNew(@MappingTarget BranchEntity entity) {
        entity.setNew(true);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
    }
}
