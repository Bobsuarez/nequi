package co.com.pragma.usecase.branch;

import co.com.pragma.model.product.KnownBranchFranchise;
import co.com.pragma.model.product.gateways.KnownBranchFranchiseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Caso de uso idempotente: sincroniza la relación branch–franchise desde el evento RabbitMQ.
 * Si el branch ya existe se actualiza (UPSERT). No falla ni llama a otros servicios.
 */
@Log
@RequiredArgsConstructor
public class SyncBranchFromEventUseCase {

    private final KnownBranchFranchiseRepository knownBranchFranchiseRepository;

    public Mono<Void> execute(KnownBranchFranchise knownBranchFranchise) {
        return Mono.defer(() ->
                                  knownBranchFranchiseRepository.saveOrUpdate(knownBranchFranchise)
                                          .doOnSuccess(ignored -> log.info("Branch sincronizado branchId=" +
                                                                                   knownBranchFranchise.getBranchId()
                                                                                           .value() +
                                                                                   " franchiseId={}" +
                                                                                   knownBranchFranchise.getFranchiseId()))
                                          .then()
        );
    }
}
