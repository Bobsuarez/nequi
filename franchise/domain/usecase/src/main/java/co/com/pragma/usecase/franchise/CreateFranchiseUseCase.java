package co.com.pragma.usecase.franchise;

import co.com.pragma.model.franchise.gateways.DomainEventPublisher;
import co.com.pragma.model.exceptions.BusinessException;
import co.com.pragma.model.franchise.Franchise;
import co.com.pragma.model.franchise.gateways.FranchiseRepository;
import co.com.pragma.model.franchise.FranchiseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

@Log
@RequiredArgsConstructor
public class CreateFranchiseUseCase {

    private static final String FRANCHISE_ALREADY_EXISTS = "Ya existe una franquicia con el nombre '%s'";

    private final FranchiseRepository franchiseRepository;
    private final DomainEventPublisher eventPublisher;

    public Mono<Franchise> execute(Franchise franchise) {

        return Mono.defer(() -> {
            String name = franchise.getName();
            log.info("Iniciando creación de franquicia name=" + name);

            return validateNameAvailable(name)
                    .then(createAndPersist(name))
                    .doOnNext(f -> log.info("Franquicia creada exitosamente id="+f.getId().value()+" name=" +f.getName()))
                    .doOnError(e -> log.info("Error al crear franquicia name={} message={}"+ name + e.getMessage()));
        });
    }

    private Mono<Void> validateNameAvailable(String name) {
        return franchiseRepository.existsByName(name)
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(
                        new BusinessException(FRANCHISE_ALREADY_EXISTS.formatted(name))
                ))
                .then();
    }

    private Mono<Franchise> createAndPersist(String name) {
        return Mono.fromSupplier(() -> Franchise.create(name))
                .flatMap(franchiseRepository::save);
    }
}
