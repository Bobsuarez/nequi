package co.com.pragma.r2dbc.repositories;

import co.com.pragma.r2dbc.entity.BranchEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface BranchR2dbcCrudRepository extends ReactiveCrudRepository<BranchEntity, UUID> {
}
