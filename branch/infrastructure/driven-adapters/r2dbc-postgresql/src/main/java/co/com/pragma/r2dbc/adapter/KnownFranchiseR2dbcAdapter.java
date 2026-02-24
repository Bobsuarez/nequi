package co.com.pragma.r2dbc.adapter;

import co.com.pragma.model.branch.KnownFranchise;
import co.com.pragma.model.branch.gateways.KnownFranchiseRepository;
import co.com.pragma.r2dbc.entity.KnownFranchiseEntity;
import co.com.pragma.r2dbc.repositories.KnownFranchiseR2dbcCrudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class KnownFranchiseR2dbcAdapter implements KnownFranchiseRepository {

    private final KnownFranchiseR2dbcCrudRepository crudRepository;

    @Override
    public Mono<Boolean> existsById(UUID franchiseId) {
        return crudRepository.existsById(franchiseId)
                .doOnNext(exists -> log.debug("existsById franchiseId={} result={}", franchiseId, exists));
    }

    @Override
    public Mono<KnownFranchise> save(KnownFranchise knownFranchise) {
        KnownFranchiseEntity entity = new KnownFranchiseEntity();
        entity.setId(knownFranchise.getId());
        entity.setName(knownFranchise.getName());
        entity.setNew(true); // fuerza INSERT; sin esto R2DBC hace UPDATE y falla si la fila no existe
        return crudRepository.save(entity)
                .map(this::toDomain)
                .doOnSuccess(k -> log.debug("Known franchise guardada id={} name={}", k.getId(), k.getName()))
                .doOnError(e -> log.error("Error guardando known franchise id={}", knownFranchise.getId(), e));
    }

    @Override
    public Mono<Void> updateName(UUID franchiseId, String newName) {
        return crudRepository.updateName(franchiseId, newName != null ? newName.trim() : null)
                .doOnSuccess(v -> log.debug("Known franchise actualizada id={} newName={}", franchiseId, newName))
                .doOnError(e -> log.error("Error actualizando nombre known franchise id={}", franchiseId, e));
    }

    private KnownFranchise toDomain(KnownFranchiseEntity entity) {
        return KnownFranchise.of(entity.getId(), entity.getName());
    }
}
