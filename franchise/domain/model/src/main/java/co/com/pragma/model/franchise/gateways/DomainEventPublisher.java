package co.com.pragma.model.franchise.gateways;

import reactor.core.publisher.Mono;

public interface DomainEventPublisher {

    Mono<Boolean> publish(Object event);
}
