# Build context: companies/nequi (raíz del monorepo)
FROM --platform=linux/arm64 postgres:16-alpine

COPY deployment/postgres/01_create_db.sql /docker-entrypoint-initdb.d/
COPY deployment/postgres/02_init_schemas.sh /docker-entrypoint-initdb.d/
COPY franchise/deployment/schema_franchise.sql /docker-entrypoint-initdb.d/
COPY branch/deployment/schema-branch_db.sql /docker-entrypoint-initdb.d/schema_branch_db.sql
COPY product/deployment/schema_product.sql /docker-entrypoint-initdb.d/schema_product.sql

RUN chmod +x /docker-entrypoint-initdb.d/02_init_schemas.sh
