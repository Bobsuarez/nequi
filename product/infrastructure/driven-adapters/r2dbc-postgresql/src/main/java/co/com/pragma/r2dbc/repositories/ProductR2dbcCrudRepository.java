package co.com.pragma.r2dbc.repositories;

import co.com.pragma.r2dbc.entity.ProductEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductR2dbcCrudRepository extends ReactiveCrudRepository<ProductEntity, UUID> {

    @Query("SELECT * FROM products WHERE id = :id AND (deleted IS NULL OR deleted = false)")
    Mono<ProductEntity> findByIdAndNotDeleted(UUID id);
}
