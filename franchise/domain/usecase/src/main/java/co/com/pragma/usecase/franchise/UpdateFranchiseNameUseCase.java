package co.com.pragma.usecase.franchise;

import co.com.pragma.model.exceptions.BusinessException;
import co.com.pragma.model.franchise.Franchise;
import co.com.pragma.model.franchise.FranchiseId;
import co.com.pragma.model.franchise.gateways.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

@Log
@RequiredArgsConstructor
public class UpdateFranchiseNameUseCase {

    private static final String FRANCHISE_ALREADY_EXISTS = "Ya existe una franquicia con el nombre '%s'";
    private static final String FRANCHISE_NOT_FOUND = "Franquicia no encontrada";

    private final FranchiseRepository franchiseRepository;

    public Mono<Franchise> execute(FranchiseId franchiseId, String newName) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new BusinessException(FRANCHISE_NOT_FOUND)))
                .flatMap(existing -> validateNameAvailableForUpdate(newName, franchiseId)
                        .then(franchiseRepository.updateName(franchiseId, newName)))
                .doOnNext(updated -> log.info("Franquicia nombre actualizado id=" + updated.getId().value() + " name=" + updated.getName()))
                .doOnError(e -> log.severe("Error al actualizar nombre franquicia id=" + franchiseId.value() + " message=" + e.getMessage()));
    }

    private Mono<Void> validateNameAvailableForUpdate(String newName, FranchiseId excludeId) {
        return franchiseRepository.existsByNameAndIdNot(newName, excludeId)
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new BusinessException(FRANCHISE_ALREADY_EXISTS.formatted(newName))))
                .then();
    }
}
