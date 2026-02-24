package co.com.pragma.r2dbc.adapter;

import co.com.pragma.model.product.Product;
import co.com.pragma.model.product.ProductCreatedEvent;
import co.com.pragma.model.product.ProductId;
import co.com.pragma.model.product.gateways.ProductRepository;
import co.com.pragma.r2dbc.entity.OutboxEventEntity;
import co.com.pragma.r2dbc.entity.ProductEntity;
import co.com.pragma.r2dbc.mappers.OutboxEventEntityMapper;
import co.com.pragma.r2dbc.mappers.ProductEntityMapper;
import co.com.pragma.r2dbc.repositories.OutboxR2dbcCrudRepository;
import co.com.pragma.r2dbc.repositories.ProductR2dbcCrudRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductR2dbcAdapter implements ProductRepository {

    private final ProductR2dbcCrudRepository crudRepository;
    private final OutboxR2dbcCrudRepository outboxR2dbcCrudRepository;
    private final ProductEntityMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Product> save(Product product) {
        ProductEntity entity = mapper.toEntity(product);
        return crudRepository.save(entity)
                .flatMap(saved ->
                        createOutboxEntity(product.getId().value(), OutboxEventEntityMapper.EVENT_TYPE_PRODUCT_CREATED,
                                ProductCreatedEvent.of(product))
                                .flatMap(outboxR2dbcCrudRepository::save)
                                .thenReturn(saved))
                .map(mapper::toDomain)
                .doOnSuccess(p -> log.debug("Producto creado id={}", p.getId().value()))
                .doOnError(e -> log.error("Error creando producto", e));
    }

    @Override
    public Mono<Product> findById(ProductId id) {
        return crudRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Product> update(Product product, Object event) {
        ProductEntity entity = mapper.toEntity(product);
        entity.setNew(false);
        return crudRepository.save(entity)
                .flatMap(saved ->
                        createOutboxEntity(product.getId().value(), eventTypeFor(event), event)
                                .flatMap(outboxR2dbcCrudRepository::save)
                                .thenReturn(saved))
                .map(mapper::toDomain)
                .doOnSuccess(p -> log.debug("Producto actualizado id={}", p.getId().value()))
                .doOnError(e -> log.error("Error actualizando producto", e));
    }

    @Override
    public Mono<Void> deleteById(ProductId id) {
        return crudRepository.deleteById(id.value())
                .flatMap(saved ->
                                 createOutboxEntity(
                                         UUID.randomUUID(), OutboxEventEntityMapper.EVENT_TYPE_PRODUCT_DELETED,
                                         Product.builder().id(id).build())
                                         .flatMap(outboxR2dbcCrudRepository::save)
                                         .thenReturn(saved))
                .then();
    }

    private Mono<OutboxEventEntity> createOutboxEntity(java.util.UUID aggregateId, String eventType, Object event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .map(payload -> OutboxEventEntityMapper.toEntity(aggregateId, eventType, payload));
    }

    private static String eventTypeFor(Object event) {
        if (event instanceof co.com.pragma.model.product.ProductDeletedEvent) {
            return OutboxEventEntityMapper.EVENT_TYPE_PRODUCT_DELETED;
        }
        if (event instanceof co.com.pragma.model.product.ProductStockUpdatedEvent) {
            return OutboxEventEntityMapper.EVENT_TYPE_PRODUCT_STOCK_UPDATED;
        }
        if (event instanceof co.com.pragma.model.product.ProductRenamedEvent) {
            return OutboxEventEntityMapper.EVENT_TYPE_PRODUCT_RENAMED;
        }
        return "UnknownEvent";
    }
}
