package co.com.pragma.model.product.gateways;

import co.com.pragma.model.product.KnownBranchFranchise;
import reactor.core.publisher.Mono;

/**
 * Puerto para procesar eventos de sincronización de sucursal (consumidor RabbitMQ).
 * La implementación delega en el caso de uso correspondiente.
 */
public interface BranchSyncHandler {

    Mono<Void> handle(KnownBranchFranchise knownBranchFranchise);
}
