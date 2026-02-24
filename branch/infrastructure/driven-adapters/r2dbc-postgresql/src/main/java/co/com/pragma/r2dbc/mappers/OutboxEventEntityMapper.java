package co.com.pragma.r2dbc.mappers;

import co.com.pragma.model.branch.Branch;
import co.com.pragma.r2dbc.entity.OutboxEventEntity;
import io.r2dbc.postgresql.codec.Json;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class OutboxEventEntityMapper {

    public static final String EVENT_TYPE_BRANCH_CREATED = "BranchCreated";
    public static final String EVENT_TYPE_BRANCH_NAME_UPDATED = "BranchNameUpdated";

    public static OutboxEventEntity toEntity(@NonNull Branch branch, String payload) {
        return toEntity(branch.getId().value(), EVENT_TYPE_BRANCH_CREATED, payload);
    }

    public static OutboxEventEntity toEntity(@NonNull UUID aggregateId, @NonNull String eventType, @NonNull String payload) {
        return OutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(Json.of(payload))
                .createdAt(LocalDateTime.now())
                .published(false)
                .isNew(true)
                .build();
    }
}
