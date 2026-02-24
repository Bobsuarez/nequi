package co.com.pragma.model.franchise.gateways;

import co.com.pragma.model.franchise.Franchise;
import co.com.pragma.model.franchise.FranchiseId;
import reactor.core.publisher.Mono;

public interface FranchiseRepository {

    Mono<Boolean> existsByName(String name);

    Mono<Boolean> existsByNameAndIdNot(String name, FranchiseId id);

    Mono<Franchise> findById(FranchiseId id);

    Mono<Franchise> save(Franchise franchise);

    Mono<Franchise> updateName(FranchiseId id, String newName);
}
