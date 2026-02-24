package co.com.pragma.validator.mappers;

import co.com.pragma.model.product.TopProductByBranch;
import co.com.pragma.validator.dto.response.TopProductByBranchResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FranchiseApiMapper {

    TopProductByBranchResponse toResponse(TopProductByBranch top);
}
