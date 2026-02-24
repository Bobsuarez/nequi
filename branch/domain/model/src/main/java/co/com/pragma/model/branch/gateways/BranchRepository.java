package co.com.pragma.model.branch.gateways;

import co.com.pragma.model.branch.Branch;
import co.com.pragma.model.branch.BranchId;
import reactor.core.publisher.Mono;

public interface BranchRepository {

    Mono<Branch> save(Branch branch);

    Mono<Branch> findById(BranchId id);

    Mono<Branch> updateName(BranchId id, String newName);
}
