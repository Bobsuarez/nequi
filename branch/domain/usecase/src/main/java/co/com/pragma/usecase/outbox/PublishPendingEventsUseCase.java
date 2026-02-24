package co.com.pragma.usecase.outbox;

import co.com.pragma.model.branch.gateways.DomainEventPublisher;
import co.com.pragma.model.branch.gateways.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

@Log
@RequiredArgsConstructor
public class PublishPendingEventsUseCase {

    private final OutboxRepository outboxRepository;
    private final DomainEventPublisher eventPublisher;

    /**
     * Procesa un lote de eventos pendientes del outbox:
     * 1. Consulta los no publicados (limitado por batchSize)
     * 2. Publica cada uno en el broker
     * 3. Lo marca como publicado en BD
     * Si un evento falla individualmente, se omite y se reintentará en el siguiente ciclo.
     */
    public Mono<Void> execute(int batchSize) {
        return outboxRepository.findUnpublished(batchSize)
                .flatMap(outboxEvent ->
                        eventPublisher.publish(outboxEvent.event())
                                .then(outboxRepository.markAsPublished(outboxEvent.id()))
                                .doOnSuccess(v -> log.info(
                                        "Outbox event publicado y marcado id=" + outboxEvent.id()
                                                + " eventType=" + outboxEvent.event().getClass().getSimpleName()))
                                .doOnError(e -> log.severe(
                                        "Error procesando outbox event id=" + outboxEvent.id() + " : " + e.getMessage()))
                                .onErrorResume(e -> Mono.empty())
                )
                .then();
    }
}
