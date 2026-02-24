package co.com.pragma.model.product;

import java.util.UUID;

/**
 * Evento pendiente de publicación leído desde la tabla outbox.
 * event puede ser ProductCreatedEvent, ProductDeletedEvent, etc.
 */
public record OutboxEvent(UUID id, Object event) {
}
