package co.com.pragma.model.branch.gateways;

import co.com.pragma.model.branch.KnownFranchise;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface KnownFranchiseRepository {

    Mono<Boolean> existsById(UUID franchiseId);

    Mono<KnownFranchise> save(KnownFranchise knownFranchise);

    Mono<Void> updateName(UUID franchiseId, String newName);
}
