package co.com.pragma.model.branch.gateways;

import reactor.core.publisher.Mono;

public interface DomainEventPublisher {

    Mono<Void> publish(Object event);
}
