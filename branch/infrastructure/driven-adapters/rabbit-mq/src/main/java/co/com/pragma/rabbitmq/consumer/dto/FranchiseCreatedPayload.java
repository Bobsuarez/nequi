package co.com.pragma.rabbitmq.consumer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload recibido cuando se crea una nueva franquicia (routing key branch.#).
 * Ejemplo: {"franchiseId":"...","name":"Sucursal NORTE","occurredOn":"2026-02-23T05:14:46.658729Z"}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FranchiseCreatedPayload(
        UUID franchiseId,
        String name,
        Instant occurredOn
) {}
