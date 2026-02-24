package co.com.pragma.usecase.branch;

import co.com.pragma.model.branch.BranchId;
import co.com.pragma.model.branch.gateways.BranchRepository;
import co.com.pragma.model.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

@Log
@RequiredArgsConstructor
public class UpdateBranchNameUseCase {

    private static final String BRANCH_NOT_FOUND = "Sucursal no encontrada";

    private final BranchRepository branchRepository;

    public Mono<co.com.pragma.model.branch.Branch> execute(BranchId branchId, String newName) {
        return branchRepository.findById(branchId)
                .switchIfEmpty(Mono.error(new BusinessException(BRANCH_NOT_FOUND)))
                .flatMap(existing -> branchRepository.updateName(branchId, newName.trim()))
                .doOnNext(updated -> log.info("Sucursal nombre actualizado id=" + updated.getId().value() + " name=" + updated.getName()))
                .doOnError(e -> log.severe("Error al actualizar nombre sucursal id=" + branchId.value() + " message=" + e.getMessage()));
    }
}
