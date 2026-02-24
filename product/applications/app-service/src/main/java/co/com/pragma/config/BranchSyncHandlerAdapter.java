package co.com.pragma.config;

import co.com.pragma.model.product.KnownBranchFranchise;
import co.com.pragma.model.product.gateways.BranchSyncHandler;
import co.com.pragma.usecase.branch.SyncBranchFromEventUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Implementación del puerto BranchSyncHandler: delega en SyncBranchFromEventUseCase.
 * Conecta el consumer RabbitMQ (módulo rabbit-mq) con el caso de uso.
 */
@Component
@Primary
@RequiredArgsConstructor
public class BranchSyncHandlerAdapter implements BranchSyncHandler {

    private final SyncBranchFromEventUseCase syncBranchFromEventUseCase;

    @Override
    public Mono<Void> handle(KnownBranchFranchise knownBranchFranchise) {
        return syncBranchFromEventUseCase.execute(knownBranchFranchise);
    }
}
