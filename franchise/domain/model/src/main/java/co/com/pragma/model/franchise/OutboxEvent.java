package co.com.pragma.model.franchise;

import java.util.UUID;

/**
 * Representa un evento pendiente de publicación leído desde la tabla outbox.
 * El id se usa para marcarlo como publicado una vez enviado al broker.
 * event puede ser FranchiseEvent, FranchiseNameUpdatedEvent, etc.
 */
public record OutboxEvent(UUID id, Object event) {
}
