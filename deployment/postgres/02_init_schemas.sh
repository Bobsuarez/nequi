#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 -d main_nequi_db -c "CREATE SCHEMA IF NOT EXISTS franchise_db; CREATE SCHEMA IF NOT EXISTS branch_db; CREATE SCHEMA IF NOT EXISTS product_db;"

psql -v ON_ERROR_STOP=1 -d main_nequi_db -f /docker-entrypoint-initdb.d/schema_franchise.sql
psql -v ON_ERROR_STOP=1 -d main_nequi_db -f /docker-entrypoint-initdb.d/schema_branch_db.sql
psql -v ON_ERROR_STOP=1 -d main_nequi_db -f /docker-entrypoint-initdb.d/schema_product.sql
