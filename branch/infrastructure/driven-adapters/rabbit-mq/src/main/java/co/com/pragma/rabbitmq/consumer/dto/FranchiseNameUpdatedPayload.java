package co.com.pragma.rabbitmq.consumer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload recibido cuando se actualiza el nombre de una franquicia (routing key branch.#).
 * Ejemplo: {"franchiseId":"...","previousName":"...","newName":"...","occurredOn":"..."}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FranchiseNameUpdatedPayload(
        UUID franchiseId,
        String previousName,
        String newName,
        Instant occurredOn
) {}
