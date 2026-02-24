package co.com.pragma.r2dbc.repositories;

import co.com.pragma.r2dbc.entity.FranchiseEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FranchiseR2dbcCrudRepository extends ReactiveCrudRepository<FranchiseEntity, UUID> {

    Mono<Boolean> existsByName(String name);

    @Query("SELECT EXISTS(SELECT 1 FROM franchises WHERE name = :name AND id != :id)")
    Mono<Boolean> existsByNameAndIdNot(String name, UUID id);
}
