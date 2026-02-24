package co.com.pragma.r2dbc.repositories;

import co.com.pragma.r2dbc.entity.OutboxEventEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OutboxR2dbcCrudRepository extends ReactiveCrudRepository<OutboxEventEntity, UUID> {

    @Query("""
        SELECT * FROM outbox_events
        WHERE published = false
        ORDER BY created_at
        LIMIT :batchSize
    """)
    Flux<OutboxEventEntity> findUnpublished(int batchSize);

    @Modifying
    @Query("UPDATE outbox_events SET published = true WHERE id = :id")
    Mono<Void> markAsPublished(UUID id);
}
