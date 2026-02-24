package co.com.pragma.validator.mappers;

import co.com.pragma.model.branch.Branch;
import co.com.pragma.model.branch.BranchId;
import co.com.pragma.validator.dto.request.CreateBranchRequest;
import co.com.pragma.validator.dto.response.BranchResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface BranchApiMapper {

    default Branch toDomain(CreateBranchRequest request) {
        if (request == null) {
            return null;
        }
        return Branch.create(request.franchiseId(), request.name());
    }

    @Mapping(target = "id", expression = "java(branch.getId() != null ? branch.getId().value() : null)")
    BranchResponse toResponse(Branch branch);
}
