package co.com.pragma.rabbitmq;

import co.com.pragma.model.branch.gateways.DomainEventPublisher;
import co.com.pragma.rabbitmq.config.RabbitMQProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

@Service
@Log4j2
@RequiredArgsConstructor
public class RabbitMQEventPublisher implements DomainEventPublisher {

    private final Sender sender;
    private final RabbitMQProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> publish(Object event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(event))
                .map(body -> new OutboundMessage(properties.exchange(), properties.routingKey(), body))
                .flatMap(message -> sender.send(Mono.just(message)))
                .doOnSuccess(v -> log.info("Evento publicado en RabbitMQ exchange={} routingKey={}",
                        properties.exchange(), properties.routingKey()))
                .doOnError(e -> log.error("Error al publicar evento en RabbitMQ ", e));
    }
}
