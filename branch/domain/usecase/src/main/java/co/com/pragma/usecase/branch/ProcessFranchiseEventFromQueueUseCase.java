package co.com.pragma.usecase.branch;

import co.com.pragma.model.branch.KnownFranchise;
import co.com.pragma.model.branch.gateways.FranchiseEventFromQueueHandler;
import co.com.pragma.model.branch.gateways.KnownFranchiseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log
@RequiredArgsConstructor
public class ProcessFranchiseEventFromQueueUseCase implements FranchiseEventFromQueueHandler {

    private final KnownFranchiseRepository knownFranchiseRepository;

    @Override
    public Mono<KnownFranchise> handleFranchiseCreated(UUID franchiseId, String name) {
        return Mono.defer(() -> {
            KnownFranchise knownFranchise = KnownFranchise.of(franchiseId, name);
            return knownFranchiseRepository.save(knownFranchise)
                    .doOnSuccess(k -> log.info("Franquicia conocida creada desde cola id="+franchiseId+" name="+  name))
                    .doOnError(e -> log.severe("Error procesando franchise created desde cola franchiseId=" + franchiseId + " " + e.getMessage()));
        });
    }

    @Override
    public Mono<Void> handleFranchiseNameUpdated(UUID franchiseId, String newName) {
        return knownFranchiseRepository.updateName(franchiseId, newName)
                .doOnSuccess(v -> log.info("Franquicia conocida actualizada desde cola id="+franchiseId+" newName="+ newName))
                .doOnError(e -> log.severe("Error procesando franchise name updated desde cola franchiseId=" + franchiseId + " " + e.getMessage()));
    }
}
