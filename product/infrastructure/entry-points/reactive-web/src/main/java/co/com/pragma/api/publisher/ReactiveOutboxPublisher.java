package co.com.pragma.api.publisher;

import co.com.pragma.usecase.outbox.SyncTopProductsFromOutboxUseCase;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * Proceso automático que lee eventos del outbox y sincroniza en top_products_by_branch.
 * Ya no publica a RabbitMQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveOutboxPublisher {

    private final SyncTopProductsFromOutboxUseCase syncTopProductsFromOutboxUseCase;

    @Value("${app.outbox.polling-interval-seconds:2}")
    private long intervalSeconds;

    @Value("${app.outbox.batch-size:50}")
    private int batchSize;

    private Disposable subscription;

    @PostConstruct
    public void start() {
        log.info("Iniciando sincronización outbox -> top_products_by_branch interval={}s batchSize={}", intervalSeconds, batchSize);
        subscription = Flux.interval(Duration.ofSeconds(intervalSeconds))
                .flatMap(tick -> syncTopProductsFromOutboxUseCase.execute(batchSize))
                .onErrorContinue((e, o) -> log.error("Error en ciclo de sincronización outbox, reintentando próximo tick", e))
                .subscribe();
    }

    @PreDestroy
    public void stop() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("ReactiveOutboxPublisher (sync top_products_by_branch) detenido");
        }
    }
}
