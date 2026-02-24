package co.com.pragma.usecase.branch;

import co.com.pragma.model.branch.Branch;
import co.com.pragma.model.branch.gateways.BranchRepository;
import co.com.pragma.model.branch.gateways.KnownFranchiseRepository;
import co.com.pragma.model.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log
@RequiredArgsConstructor
public class CreateBranchUseCase {

    private static final String FRANCHISE_NOT_FOUND = "La franquicia no existe o no está registrada";

    private final KnownFranchiseRepository knownFranchiseRepository;
    private final BranchRepository branchRepository;

    public Mono<Branch> execute(Branch branch) {
        return Mono.defer(() -> {
            UUID franchiseId = branch.getFranchiseId();
            log.info("Iniciando creación de sucursal franchiseId=" + franchiseId + " name=" + branch.getName());

            return validateFranchiseExists(franchiseId)
                    .then(Mono.fromSupplier(() -> branch))
                    .flatMap(branchRepository::save)
                    .doOnNext(b -> log.info("Sucursal creada exitosamente id=" + b.getId().value() + " name=" + b.getName()))
                    .doOnError(e -> log.severe("Error al crear sucursal franchiseId=" + franchiseId + " message=" + e.getMessage()));
        });
    }

    private Mono<Void> validateFranchiseExists(UUID franchiseId) {
        return knownFranchiseRepository.existsById(franchiseId)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new BusinessException(FRANCHISE_NOT_FOUND)))
                .then();
    }
}
