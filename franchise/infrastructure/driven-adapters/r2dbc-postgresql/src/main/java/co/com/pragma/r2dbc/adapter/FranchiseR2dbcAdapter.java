package co.com.pragma.r2dbc.adapter;

import co.com.pragma.model.exceptions.BusinessException;
import co.com.pragma.model.franchise.Franchise;
import co.com.pragma.model.franchise.FranchiseEvent;
import co.com.pragma.model.franchise.FranchiseId;
import co.com.pragma.model.franchise.FranchiseNameUpdatedEvent;
import co.com.pragma.model.franchise.gateways.FranchiseRepository;
import co.com.pragma.r2dbc.entity.FranchiseEntity;
import co.com.pragma.r2dbc.entity.OutboxEventEntity;
import co.com.pragma.r2dbc.mappers.FranchiseEntityMapper;
import co.com.pragma.r2dbc.mappers.OutboxEventEntityMapper;
import co.com.pragma.r2dbc.repositories.FranchiseR2dbcCrudRepository;
import co.com.pragma.r2dbc.repositories.OutboxR2dbcCrudRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FranchiseR2dbcAdapter implements FranchiseRepository {

    private static final String FRANCHISE_NOT_FOUND = "Franquicia no encontrada";

    private final FranchiseR2dbcCrudRepository crudRepository;
    private final OutboxR2dbcCrudRepository outboxR2dbcCrudRepository;
    private final FranchiseEntityMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Boolean> existsByName(String name) {
        return crudRepository.existsByName(name)
                .doOnNext(exists -> log.debug("existsByName name={} result={}", name, exists));
    }

    @Override
    public Mono<Boolean> existsByNameAndIdNot(String name, FranchiseId id) {
        return crudRepository.existsByNameAndIdNot(name, id.value())
                .doOnNext(
                        exists -> log.debug("existsByNameAndIdNot name={} id={} result={}", name, id.value(), exists));
    }

    @Override
    public Mono<Franchise> findById(FranchiseId id) {
        return crudRepository.findById(id.value())
                .map(mapper::toDomain)
                .doOnNext(f -> log.debug("findById id={}", id.value()));
    }

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return crudRepository.save(mapper.toEntity(franchise))
                .flatMap(savedEntity ->
                                 Mono.fromCallable(() -> objectMapper.writeValueAsString(FranchiseEvent.of(franchise)))
                                         .map(payload -> OutboxEventEntityMapper.toEntity(franchise, payload))
                                         .flatMap(outboxR2dbcCrudRepository::save)
                                         .thenReturn(savedEntity))
                .map(mapper::toDomain)
                .doOnSuccess(saved -> log.debug(
                        "Franquicia persistida id={}", saved.getId()
                                .value()
                ))
                .doOnError(error -> log.error(
                        "Error al persistir franquicia name={} message={}", franchise.getName(),
                        error.getMessage(), error
                ));
    }

    @Override
    public Mono<Franchise> updateName(FranchiseId id, String newName) {

        return crudRepository.findById(id.value())
                .switchIfEmpty(Mono.error(new BusinessException(FRANCHISE_NOT_FOUND)))
                .flatMap(entity -> applyNameChange(entity, newName))
                .flatMap(this::saveWithOutbox)
                .map(mapper::toDomain)
                .doOnSuccess(f -> log.debug("Franquicia nombre actualizado id={} newName={}", id.value(), newName))
                .doOnError(e -> log.error(
                        "Error al actualizar nombre franquicia id={} message={}", id.value(),
                        e.getMessage(), e
                ));
    }

    private Mono<FranchiseEntity> applyNameChange(FranchiseEntity entity, String newName) {
        String previousName = entity.getName();
        if (previousName.equals(newName)) {
            return Mono.just(entity); // idempotencia real
        }
        entity.setNamePrevious(previousName);
        entity.setName(newName);
        return Mono.just(entity);
    }

    private Mono<FranchiseEntity> saveWithOutbox(FranchiseEntity entity) {
        return crudRepository.save(entity)
                .flatMap(saved ->
                                 createOutboxEvent(saved)
                                         .flatMap(outboxR2dbcCrudRepository::save)
                                         .thenReturn(saved)
                );
    }

    private Mono<OutboxEventEntity> createOutboxEvent(FranchiseEntity saved) {

        FranchiseNameUpdatedEvent event =
                new FranchiseNameUpdatedEvent(
                        saved.getId(),
                        saved.getNamePrevious(),
                        saved.getName(),
                        Instant.now()
                );

        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .subscribeOn(Schedulers.boundedElastic())
                .map(payload -> OutboxEventEntityMapper.toEntity(
                        saved.getId(),
                        OutboxEventEntityMapper.EVENT_TYPE_FRANCHISE_NAME_UPDATED,
                        payload
                ));
    }
}
