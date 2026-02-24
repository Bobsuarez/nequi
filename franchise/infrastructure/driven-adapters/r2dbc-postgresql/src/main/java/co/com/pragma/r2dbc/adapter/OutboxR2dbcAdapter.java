package co.com.pragma.r2dbc.adapter;

import co.com.pragma.model.franchise.FranchiseEvent;
import co.com.pragma.model.franchise.FranchiseNameUpdatedEvent;
import co.com.pragma.model.franchise.OutboxEvent;
import co.com.pragma.model.franchise.gateways.OutboxRepository;
import co.com.pragma.r2dbc.mappers.OutboxEventEntityMapper;
import co.com.pragma.r2dbc.repositories.OutboxR2dbcCrudRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OutboxR2dbcAdapter implements OutboxRepository {

    private final OutboxR2dbcCrudRepository crudRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Flux<OutboxEvent> findUnpublished(int batchSize) {
        return crudRepository.findUnpublished(batchSize)
                .flatMap(entity -> Mono.fromCallable(() -> {
                    String json = new String(entity.getPayload().asArray(), StandardCharsets.UTF_8);
                    Object event = deserializeByEventType(entity.getEventType(), json);
                    return new OutboxEvent(entity.getId(), event);
                }))
                .doOnNext(e -> log.debug("Outbox event leído id={} eventType={}", e.id(), e.event().getClass().getSimpleName()))
                .doOnError(err -> log.error("Error deserializando outbox event: {}", err.getMessage(), err));
    }

    private Object deserializeByEventType(String eventType, String json) throws JsonProcessingException {
        return switch (eventType) {
            case OutboxEventEntityMapper.EVENT_TYPE_FRANCHISE_CREATED -> objectMapper.readValue(json, FranchiseEvent.class);
            case OutboxEventEntityMapper.EVENT_TYPE_FRANCHISE_NAME_UPDATED -> objectMapper.readValue(json, FranchiseNameUpdatedEvent.class);
            default -> throw new IllegalArgumentException("Tipo de evento outbox no soportado: " + eventType);
        };
    }

    @Override
    public Mono<Void> markAsPublished(UUID id) {
        return crudRepository.markAsPublished(id)
                .doOnSuccess(v -> log.debug("Outbox event marcado como publicado id={}", id));
    }
}
