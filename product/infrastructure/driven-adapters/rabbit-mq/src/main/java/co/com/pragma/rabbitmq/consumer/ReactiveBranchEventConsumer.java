package co.com.pragma.rabbitmq.consumer;

import co.com.pragma.model.product.KnownBranchFranchise;
import co.com.pragma.model.product.gateways.BranchSyncHandler;
import co.com.pragma.rabbitmq.config.RabbitMQProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.Receiver;

import java.nio.charset.StandardCharsets;

/**
 * Consumer reactivo de RabbitMQ que procesa eventos de sucursal.
 * Idempotente: delega en BranchSyncHandler (UPSERT en known_branch_franchise).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveBranchEventConsumer {

    private final Receiver receiver;
    private final RabbitMQProperties rabbitMQProperties;
    private final BranchSyncHandler branchSyncHandler;
    private final ObjectMapper objectMapper;

    private Disposable subscription;

    @PostConstruct
    public void start() {
        if (rabbitMQProperties.consumer() == null || rabbitMQProperties.consumer().queueName() == null
                || rabbitMQProperties.consumer().queueName().isBlank()) {
            log.warn("RabbitMQ consumer queue no configurada, no se inicia el consumer de branch");
            return;
        }
        String queueName = rabbitMQProperties.consumer().queueName();
        log.info("Iniciando consumer reactivo de branch queue={}", queueName);

        subscription = receiver.consumeAutoAck(queueName)
                .flatMap(delivery -> {
                    String body = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    return parseAndSync(body)
                            .doOnSuccess(v -> log.debug("Evento branch procesado"))
                            .doOnError(e -> log.error("Error procesando evento branch body={}", body, e))
                            .onErrorResume(e -> Mono.empty());
                })
                .subscribe();
    }

    @PreDestroy
    public void stop() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("ReactiveBranchEventConsumer detenido");
        }
    }

    private Mono<Void> parseAndSync(String json) {
        return Mono.fromCallable(() -> objectMapper.readValue(json, BranchSyncEventPayload.class))
                .subscribeOn(Schedulers.boundedElastic())
                .map(this::toKnownBranchFranchise)
                .flatMap(branchSyncHandler::handle);
    }

    private KnownBranchFranchise toKnownBranchFranchise(BranchSyncEventPayload payload) {
        return KnownBranchFranchise.of(
                payload.branchId(),
                payload.name(),
                payload.franchiseId(),
                payload.occurredOn() != null ? payload.occurredOn() : java.time.Instant.now());
    }
}
