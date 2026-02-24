package co.com.pragma.r2dbc.adapter;

import co.com.pragma.model.product.OutboxEvent;
import co.com.pragma.model.product.ProductCreatedEvent;
import co.com.pragma.model.product.ProductDeletedEvent;
import co.com.pragma.model.product.ProductRenamedEvent;
import co.com.pragma.model.product.ProductStockUpdatedEvent;
import co.com.pragma.model.product.gateways.OutboxRepository;
import co.com.pragma.r2dbc.entity.OutboxEventEntity;
import co.com.pragma.r2dbc.mappers.OutboxEventEntityMapper;
import co.com.pragma.r2dbc.repositories.OutboxR2dbcCrudRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OutboxR2dbcAdapter implements OutboxRepository {

    private final OutboxR2dbcCrudRepository crudRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Flux<OutboxEvent> findUnpublished(int batchSize) {
        return crudRepository.findUnpublished(batchSize)
                .flatMap(entity -> Mono.fromCallable(() -> toOutboxEvent(entity))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    @Override
    public Mono<Void> markAsPublished(java.util.UUID id) {
        return crudRepository.markAsPublished(id);
    }

    private OutboxEvent toOutboxEvent(OutboxEventEntity entity) {
        String json = entity.getPayload() != null
                ? new String(entity.getPayload().asArray(), StandardCharsets.UTF_8)
                : "{}";
        Object event = deserializeEvent(entity.getEventType(), json);
        return OutboxEventEntityMapper.toDomain(entity, event);
    }

    private Object deserializeEvent(String eventType, String json) {
        try {
            return switch (eventType) {
                case OutboxEventEntityMapper.EVENT_TYPE_PRODUCT_CREATED ->
                        objectMapper.readValue(json, ProductCreatedEvent.class);
                case OutboxEventEntityMapper.EVENT_TYPE_PRODUCT_DELETED ->
                        objectMapper.readValue(json, ProductDeletedEvent.class);
                case OutboxEventEntityMapper.EVENT_TYPE_PRODUCT_STOCK_UPDATED ->
                        objectMapper.readValue(json, ProductStockUpdatedEvent.class);
                case OutboxEventEntityMapper.EVENT_TYPE_PRODUCT_RENAMED ->
                        objectMapper.readValue(json, ProductRenamedEvent.class);
                default -> objectMapper.readTree(json);
            };
        } catch (Exception e) {
            log.warn("No se pudo deserializar evento type={} json={}", eventType, json, e);
            return json;
        }
    }
}
