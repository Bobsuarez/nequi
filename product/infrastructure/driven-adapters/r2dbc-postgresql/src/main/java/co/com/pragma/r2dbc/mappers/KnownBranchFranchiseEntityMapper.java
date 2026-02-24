package co.com.pragma.r2dbc.mappers;

import co.com.pragma.model.product.BranchId;
import co.com.pragma.model.product.KnownBranchFranchise;
import co.com.pragma.r2dbc.entity.KnownBranchFranchiseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface KnownBranchFranchiseEntityMapper {

    @Mapping(target = "branchId", source = "branchId.value")
    KnownBranchFranchiseEntity toEntity(KnownBranchFranchise domain);

    @Mapping(target = "branchId", expression = "java(mapBranchId(entity.getBranchId()))")
    KnownBranchFranchise toDomain(KnownBranchFranchiseEntity entity);

    default BranchId mapBranchId(UUID value) {
        return value != null ? BranchId.of(value) : null;
    }
}
