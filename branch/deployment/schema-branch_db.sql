-- Schema branch_db para el servicio de sucursales (branches)
-- Ejecutar en PostgreSQL con schema branch_db (configurado en application.yaml: spring.datasource.schema)

CREATE TABLE IF NOT EXISTS branch_db.known_franchises (
    id UUID PRIMARY KEY,
    name VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS branch_db.branches (
    id              UUID PRIMARY KEY,
    franchise_id    UUID NOT NULL,
    name            VARCHAR(150) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS branch_db.outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL,
    published BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_outbox_unpublished
    ON branch_db.outbox_events (published, created_at)
    WHERE published = false;
