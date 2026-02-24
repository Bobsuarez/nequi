package co.com.pragma.rabbitmq.consumer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload del evento de sucursal recibido por RabbitMQ.
 */
public record BranchSyncEventPayload(
        @JsonProperty("branchId") UUID branchId,
        @JsonProperty("franchiseId") UUID franchiseId,
        @JsonProperty("name") String name,
        @JsonProperty("occurredOn") Instant occurredOn
) {
    @JsonCreator
    public BranchSyncEventPayload {
    }
}
