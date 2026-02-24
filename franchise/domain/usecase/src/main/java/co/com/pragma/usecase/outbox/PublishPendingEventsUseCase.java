package co.com.pragma.usecase.outbox;

import co.com.pragma.model.franchise.gateways.DomainEventPublisher;
import co.com.pragma.model.franchise.gateways.OutboxRepository;
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
     * 3. Solo si la publicación fue exitosa, lo marca como publicado en BD (evita inconsistencia si Rabbit no está disponible)
     * Si un evento falla (ej. sin conexión a Rabbit), no se marca y se reintentará en el siguiente ciclo.
     */
    public Mono<Void> execute(int batchSize) {
        return outboxRepository.findUnpublished(batchSize)
                .flatMap(outboxEvent ->
                        eventPublisher.publish(outboxEvent.event())
                                .flatMap(published -> Boolean.TRUE.equals(published)
                                        ? outboxRepository.markAsPublished(outboxEvent.id())
                                        : Mono.<Void>empty())
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
