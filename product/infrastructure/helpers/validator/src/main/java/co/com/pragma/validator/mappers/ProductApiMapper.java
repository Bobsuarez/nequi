package co.com.pragma.validator.mappers;

import co.com.pragma.model.product.BranchId;
import co.com.pragma.model.product.Product;
import co.com.pragma.validator.dto.request.CreateProductRequest;
import co.com.pragma.validator.dto.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProductApiMapper {

    default Product toDomain(CreateProductRequest request) {
        if (request == null) {
            return null;
        }
        return Product.builder()
                .id(null)
                .branchId(BranchId.of(request.branchId()))
                .name(request.name())
                .stock(request.stock())
                .build();
    }

    @Mapping(target = "id", expression = "java(product.getId() != null ? product.getId().value() : null)")
    @Mapping(target = "branchId", expression = "java(product.getBranchId() != null ? product.getBranchId().value() : null)")
    ProductResponse toResponse(Product product);
}
