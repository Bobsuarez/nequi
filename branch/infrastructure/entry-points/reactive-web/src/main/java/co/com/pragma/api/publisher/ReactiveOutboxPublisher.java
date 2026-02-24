package co.com.pragma.api.publisher;

import co.com.pragma.usecase.outbox.PublishPendingEventsUseCase;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveOutboxPublisher {

    private final PublishPendingEventsUseCase publishPendingEventsUseCase;

    @Value("${app.outbox.polling-interval-seconds:300}")
    private long intervalSeconds;

    @Value("${app.outbox.batch-size:50}")
    private int batchSize;

    private Disposable subscription;

    @PostConstruct
    public void start() {
        log.info("Iniciando ReactiveOutboxPublisher interval={}s batchSize={}", intervalSeconds, batchSize);

        subscription = Flux.interval(Duration.ofSeconds(intervalSeconds))
                .flatMap(tick -> publishPendingEventsUseCase.execute(batchSize))
                .onErrorContinue((error, obj) ->
                        log.error("Error en ciclo de polling outbox, se continúa en el próximo tick", error))
                .subscribe();
    }

    @PreDestroy
    public void stop() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("ReactiveOutboxPublisher detenido correctamente");
        }
    }
}
