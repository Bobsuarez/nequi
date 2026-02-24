-- Schema product_db (esquema por defecto de la aplicación)
CREATE SCHEMA IF NOT EXISTS product_db;

CREATE TABLE IF NOT EXISTS product_db.products (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    stock INTEGER NOT NULL CHECK (stock >= 0),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS product_db.outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_outbox_unpublished
    ON product_db.outbox_events (published, created_at)
    WHERE published = false;

CREATE TABLE IF NOT EXISTS product_db.top_products_by_branch (
    franchise UUID NOT NULL,
    branch_id UUID PRIMARY KEY,
    product_id UUID NULL,
    product_name VARCHAR(150) NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Schema branch_db (tabla de control sincronizada desde RabbitMQ)
CREATE SCHEMA IF NOT EXISTS branch_db;

CREATE TABLE IF NOT EXISTS branch_db.known_branch_franchise (
    branch_id UUID PRIMARY KEY,
    branch_name VARCHAR(150),
    franchise_id UUID NOT NULL,
    "occurredOn" TIMESTAMP
);
