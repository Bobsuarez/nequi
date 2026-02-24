package co.com.pragma.rabbitmq.consumer;

import co.com.pragma.model.branch.gateways.FranchiseEventFromQueueHandler;
import co.com.pragma.rabbitmq.config.RabbitMQConsumerProperties;
import co.com.pragma.rabbitmq.consumer.dto.FranchiseCreatedPayload;
import co.com.pragma.rabbitmq.consumer.dto.FranchiseNameUpdatedPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Delivery;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;

import java.nio.charset.StandardCharsets;

/**
 * Consumer que solo escucha la cola y delega el procesamiento al caso de uso (puerto FranchiseEventFromQueueHandler).
 * La lógica de negocio vive en el use case; este adapter solo recibe el mensaje y lo despacha.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQFranchiseQueueConsumer {

    private final Receiver receiver;
    private final RabbitMQConsumerProperties consumerProperties;
    private final FranchiseEventFromQueueHandler franchiseEventFromQueueHandler;
    private final ObjectMapper objectMapper;

    private Disposable subscription;

    @PostConstruct
    public void start() {
        String queueName = consumerProperties.queueName();
        log.info("Iniciando RabbitMQFranchiseQueueConsumer cola={}", queueName);

        subscription = receiver.consumeAutoAck(queueName)
                .flatMap(this::processDelivery)
                .onErrorContinue((e, o) -> log.error("Error procesando mensaje de cola {}: {}", queueName, e.getMessage(), e))
                .subscribe();
    }

    @PreDestroy
    public void stop() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("RabbitMQFranchiseQueueConsumer detenido");
        }
    }

    private Mono<Void> processDelivery(Delivery delivery) {
        String payload = new String(delivery.getBody(), StandardCharsets.UTF_8);
        return parseAndDispatch(payload);
    }

    private Mono<Void> parseAndDispatch(String payload)  {
        return Mono.fromCallable(() -> objectMapper.readTree(payload))
                .flatMap(node -> {
                    if (node.has("newName") && node.has("previousName")) {
                        FranchiseNameUpdatedPayload dto = null;
                        try {
                            dto = objectMapper.treeToValue(node, FranchiseNameUpdatedPayload.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        return franchiseEventFromQueueHandler.handleFranchiseNameUpdated(dto.franchiseId(), dto.newName())
                                .then();
                    }
                    if (node.has("franchiseId") && node.has("name")) {
                        FranchiseCreatedPayload dto = null;
                        try {
                            dto = objectMapper.treeToValue(node, FranchiseCreatedPayload.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        return franchiseEventFromQueueHandler.handleFranchiseCreated(dto.franchiseId(), dto.name())
                                .then();
                    }
                    log.warn("Payload no reconocido (se ignora): {}", payload);
                    return Mono.empty();
                })
                .onErrorResume(e -> {
                    log.error("Error parseando payload: {}", e.getMessage(), e);
                    return Mono.empty();
                });
    }
}
