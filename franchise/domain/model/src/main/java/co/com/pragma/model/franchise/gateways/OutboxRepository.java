package co.com.pragma.model.franchise.gateways;

import co.com.pragma.model.franchise.OutboxEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OutboxRepository {

    Flux<OutboxEvent> findUnpublished(int batchSize);

    Mono<Void> markAsPublished(UUID id);
}
