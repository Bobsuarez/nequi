package co.com.pragma.r2dbc.mappers;

import co.com.pragma.model.product.OutboxEvent;
import co.com.pragma.model.product.Product;
import co.com.pragma.model.product.ProductCreatedEvent;
import co.com.pragma.model.product.ProductDeletedEvent;
import co.com.pragma.model.product.ProductRenamedEvent;
import co.com.pragma.model.product.ProductStockUpdatedEvent;
import co.com.pragma.r2dbc.entity.OutboxEventEntity;
import io.r2dbc.postgresql.codec.Json;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class OutboxEventEntityMapper {

    public static final String EVENT_TYPE_PRODUCT_CREATED = "ProductCreatedEvent";
    public static final String EVENT_TYPE_PRODUCT_DELETED = "ProductDeletedEvent";
    public static final String EVENT_TYPE_PRODUCT_STOCK_UPDATED = "ProductStockUpdatedEvent";
    public static final String EVENT_TYPE_PRODUCT_RENAMED = "ProductRenamedEvent";

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

    public static OutboxEvent toDomain(OutboxEventEntity entity, Object event) {
        return new OutboxEvent(entity.getId(), event);
    }
}
