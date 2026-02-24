package co.com.pragma.model.product.gateways;

import reactor.core.publisher.Mono;

public interface DomainEventPublisher {

    Mono<Boolean> publish(Object event);
}
