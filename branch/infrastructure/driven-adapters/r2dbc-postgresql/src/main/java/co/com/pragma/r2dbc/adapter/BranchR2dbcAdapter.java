package co.com.pragma.r2dbc.adapter;

import co.com.pragma.model.branch.Branch;
import co.com.pragma.model.branch.BranchEvent;
import co.com.pragma.model.branch.BranchId;
import co.com.pragma.model.branch.BranchNameUpdatedEvent;
import co.com.pragma.model.exceptions.BusinessException;
import co.com.pragma.model.branch.gateways.BranchRepository;
import co.com.pragma.r2dbc.entity.BranchEntity;
import co.com.pragma.r2dbc.entity.OutboxEventEntity;
import co.com.pragma.r2dbc.mappers.BranchEntityMapper;
import co.com.pragma.r2dbc.mappers.OutboxEventEntityMapper;
import co.com.pragma.r2dbc.repositories.BranchR2dbcCrudRepository;
import co.com.pragma.r2dbc.repositories.OutboxR2dbcCrudRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BranchR2dbcAdapter implements BranchRepository {

    private static final String BRANCH_NOT_FOUND = "Sucursal no encontrada";

    private final BranchR2dbcCrudRepository crudRepository;
    private final OutboxR2dbcCrudRepository outboxR2dbcCrudRepository;
    private final BranchEntityMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Branch> save(Branch branch) {
        return crudRepository.save(mapper.toEntity(branch))
                .flatMap(savedEntity ->
                        Mono.fromCallable(() -> objectMapper.writeValueAsString(BranchEvent.of(branch)))
                                .map(payload -> OutboxEventEntityMapper.toEntity(branch, payload))
                                .flatMap(outboxR2dbcCrudRepository::save)
                                .thenReturn(savedEntity))
                .map(mapper::toDomain)
                .doOnSuccess(saved -> log.debug("Sucursal persistida id={}", saved.getId().value()))
                .doOnError(error -> log.error("Error al persistir sucursal franchiseId={} name={} message={}",
                        branch.getFranchiseId(), branch.getName(), error.getMessage(), error));
    }

    @Override
    public Mono<Branch> findById(BranchId id) {
        return crudRepository.findById(id.value())
                .map(mapper::toDomain)
                .doOnNext(b -> log.debug("findById id={}", id.value()));
    }

    @Override
    public Mono<Branch> updateName(BranchId id, String newName) {
        return crudRepository.findById(id.value())
                .switchIfEmpty(Mono.error(new BusinessException(BRANCH_NOT_FOUND)))
                .flatMap(entity -> applyNameChange(entity, newName))
                .flatMap(this::saveWithOutbox)
                .map(mapper::toDomain)
                .doOnSuccess(b -> log.debug("Sucursal nombre actualizado id={} newName={}", id.value(), newName))
                .doOnError(e -> log.error("Error al actualizar nombre sucursal id={} message={}", id.value(), e.getMessage(), e));
    }

    private Mono<BranchEntity> applyNameChange(BranchEntity entity, String newName) {
        String previousName = entity.getName();
        if (previousName != null && previousName.equals(newName)) {
            return Mono.just(entity);
        }
        entity.setNamePrevious(previousName);
        entity.setName(newName);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setNew(false);
        return Mono.just(entity);
    }

    private Mono<BranchEntity> saveWithOutbox(BranchEntity entity) {
        return crudRepository.save(entity)
                .flatMap(saved ->
                        createOutboxEvent(saved)
                                .flatMap(outboxR2dbcCrudRepository::save)
                                .thenReturn(saved)
                );
    }

    private Mono<OutboxEventEntity> createOutboxEvent(BranchEntity saved) {
        BranchNameUpdatedEvent event = new BranchNameUpdatedEvent(
                saved.getId(),
                saved.getNamePrevious(),
                saved.getName(),
                java.time.Instant.now()
        );

        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .map(payload -> OutboxEventEntityMapper.toEntity(
                        saved.getId(),
                        OutboxEventEntityMapper.EVENT_TYPE_BRANCH_NAME_UPDATED,
                        payload
                ));
    }
}
