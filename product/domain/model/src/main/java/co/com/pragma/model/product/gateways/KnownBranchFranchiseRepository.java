package co.com.pragma.model.product.gateways;

import co.com.pragma.model.product.KnownBranchFranchise;
import reactor.core.publisher.Mono;

public interface KnownBranchFranchiseRepository {

    Mono<Boolean> existsByBranchId(java.util.UUID branchId);

    Mono<KnownBranchFranchise> findByBranchId(java.util.UUID branchId);

    Mono<KnownBranchFranchise> saveOrUpdate(KnownBranchFranchise knownBranchFranchise);
}
