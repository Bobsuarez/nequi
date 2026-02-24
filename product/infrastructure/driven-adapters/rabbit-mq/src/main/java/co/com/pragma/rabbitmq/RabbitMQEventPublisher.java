package co.com.pragma.rabbitmq;

import co.com.pragma.model.product.gateways.DomainEventPublisher;
import co.com.pragma.rabbitmq.config.RabbitMQProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQEventPublisher implements DomainEventPublisher {

    private final Sender sender;
    private final RabbitMQProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Boolean> publish(Object event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(event))
                .map(body -> new OutboundMessage(properties.exchange(), properties.routingKey(), body))
                .flatMap(message -> sender.send(Mono.just(message)))
                .thenReturn(true)
                .doOnSuccess(v -> log.debug("Evento publicado exchange={} routingKey={}", properties.exchange(), properties.routingKey()))
                .doOnError(e -> log.error("Error publicando en RabbitMQ", e))
                .onErrorReturn(false);
    }
}
