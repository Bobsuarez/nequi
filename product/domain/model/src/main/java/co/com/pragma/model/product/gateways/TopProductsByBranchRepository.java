package co.com.pragma.model.product.gateways;

import co.com.pragma.model.product.TopProductByBranch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TopProductsByBranchRepository {

    Mono<Void> saveOrUpdate(TopProductByBranch topProductByBranch);

    Flux<TopProductByBranch> findByFranchiseId(UUID franchiseId);

    Mono<Void> deleteByBranchId(UUID productId);
}
