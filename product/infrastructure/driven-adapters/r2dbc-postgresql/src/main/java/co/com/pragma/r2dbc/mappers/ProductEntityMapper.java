package co.com.pragma.r2dbc.mappers;

import co.com.pragma.model.product.BranchId;
import co.com.pragma.model.product.Product;
import co.com.pragma.model.product.ProductId;
import co.com.pragma.r2dbc.entity.ProductEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProductEntityMapper {

    @Mapping(target = "new", ignore = true)
    ProductEntity toEntity(Product product);

    Product toDomain(ProductEntity entity);

    default UUID map(ProductId id) {
        return id != null ? id.value() : null;
    }

    default ProductId mapProductId(UUID value) {
        return value != null ? ProductId.of(value) : null;
    }

    default UUID map(BranchId id) {
        return id != null ? id.value() : null;
    }

    default BranchId mapBranchId(UUID value) {
        return value != null ? BranchId.of(value) : null;
    }

    default LocalDateTime map(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneOffset.UTC) : null;
    }

    default Instant map(LocalDateTime ldt) {
        return ldt != null ? ldt.toInstant(ZoneOffset.UTC) : null;
    }

    @AfterMapping
    default void markAsNew(@MappingTarget ProductEntity entity) {
        entity.setNew(true);
    }
}
