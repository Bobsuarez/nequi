package co.com.pragma.r2dbc.repositories;

import co.com.pragma.r2dbc.entity.KnownFranchiseEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface KnownFranchiseR2dbcCrudRepository extends ReactiveCrudRepository<KnownFranchiseEntity, UUID> {

    @Modifying
    @Query("UPDATE known_franchises SET name = :name WHERE id = CAST(:id AS UUID)")
    Mono<Void> updateName(UUID id, String name);
}
