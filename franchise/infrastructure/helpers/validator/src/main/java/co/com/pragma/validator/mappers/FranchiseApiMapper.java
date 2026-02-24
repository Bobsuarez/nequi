package co.com.pragma.validator.mappers;

import co.com.pragma.model.franchise.Franchise;
import co.com.pragma.model.franchise.FranchiseId;
import co.com.pragma.validator.dto.request.CreateFranchiseRequest;
import co.com.pragma.validator.dto.respose.FranchiseResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FranchiseApiMapper {

    Franchise toDomain(CreateFranchiseRequest request);

    FranchiseResponse toResponse(Franchise franchise);

    default String map(FranchiseId id) {
        return id != null ? id.value().toString() : null;
    }
}


