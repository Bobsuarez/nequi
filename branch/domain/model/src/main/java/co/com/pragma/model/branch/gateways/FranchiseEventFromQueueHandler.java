package co.com.pragma.model.branch.gateways;

import co.com.pragma.model.branch.KnownFranchise;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Puerto (driving) para procesar eventos de franquicia recibidos desde la cola RabbitMQ.
 * El caso de uso implementa este puerto; el adapter rabbit-mq solo escucha y delega.
 */
public interface FranchiseEventFromQueueHandler {

    Mono<KnownFranchise> handleFranchiseCreated(UUID franchiseId, String name);

    Mono<Void> handleFranchiseNameUpdated(UUID franchiseId, String newName);
}
