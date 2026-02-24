package co.com.pragma.r2dbc.mappers;

import co.com.pragma.model.franchise.Franchise;
import co.com.pragma.model.franchise.FranchiseId;
import co.com.pragma.r2dbc.entity.FranchiseEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface FranchiseEntityMapper {

    @Mapping(target = "new", ignore = true)
    FranchiseEntity toEntity(Franchise franchise);

    Franchise toDomain(FranchiseEntity entity);

    default UUID map(FranchiseId franchiseId) {
        return franchiseId != null ? franchiseId.value() : null;
    }

    default FranchiseId map(UUID value) {
        return value != null ? FranchiseId.of(value) : null;
    }

    @AfterMapping
    default void markAsNew(@MappingTarget FranchiseEntity entity) {
        entity.setNew(true);
    }
}
