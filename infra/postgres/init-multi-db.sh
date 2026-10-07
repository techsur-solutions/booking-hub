#!/bin/bash
set -e

# Postgres multi-database init script for Booking-Hub
# Mounted into /docker-entrypoint-initdb.d/ - runs once on first container boot
# Creates 8 databases + 9 roles (7 simple service roles + audit_migrator + audit_svc split)

echo "Initializing Booking-Hub databases..."

# 1. booking-service
echo "Creating booking_svc role and booking_db database..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE ROLE booking_svc WITH LOGIN PASSWORD 'booking_svc_pw';
    CREATE DATABASE booking_db OWNER booking_svc;
    REVOKE ALL ON DATABASE booking_db FROM PUBLIC;
    GRANT ALL PRIVILEGES ON DATABASE booking_db TO booking_svc;
EOSQL

# 2. locations-resources-service
echo "Creating locres_svc role and locres_db database..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE ROLE locres_svc WITH LOGIN PASSWORD 'locres_svc_pw';
    CREATE DATABASE locres_db OWNER locres_svc;
    REVOKE ALL ON DATABASE locres_db FROM PUBLIC;
    GRANT ALL PRIVILEGES ON DATABASE locres_db TO locres_svc;
EOSQL

# 3. custom-field-service
echo "Creating customfld_svc role and customfld_db database..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE ROLE customfld_svc WITH LOGIN PASSWORD 'customfld_svc_pw';
    CREATE DATABASE customfld_db OWNER customfld_svc;
    REVOKE ALL ON DATABASE customfld_db FROM PUBLIC;
    GRANT ALL PRIVILEGES ON DATABASE customfld_db TO customfld_svc;
EOSQL

# 4. users-permissions-service
echo "Creating userperm_svc role and userperm_db database..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE ROLE userperm_svc WITH LOGIN PASSWORD 'userperm_svc_pw';
    CREATE DATABASE userperm_db OWNER userperm_svc;
    REVOKE ALL ON DATABASE userperm_db FROM PUBLIC;
    GRANT ALL PRIVILEGES ON DATABASE userperm_db TO userperm_svc;
EOSQL

# 5. notifications-service
echo "Creating notif_svc role and notif_db database..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE ROLE notif_svc WITH LOGIN PASSWORD 'notif_svc_pw';
    CREATE DATABASE notif_db OWNER notif_svc;
    REVOKE ALL ON DATABASE notif_db FROM PUBLIC;
    GRANT ALL PRIVILEGES ON DATABASE notif_db TO notif_svc;
EOSQL

# 6. feeds-service
echo "Creating feeds_svc role and feeds_db database..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE ROLE feeds_svc WITH LOGIN PASSWORD 'feeds_svc_pw';
    CREATE DATABASE feeds_db OWNER feeds_svc;
    REVOKE ALL ON DATABASE feeds_db FROM PUBLIC;
    GRANT ALL PRIVILEGES ON DATABASE feeds_db TO feeds_svc;
EOSQL

# 7. settings-service
echo "Creating settings_svc role and settings_db database..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE ROLE settings_svc WITH LOGIN PASSWORD 'settings_svc_pw';
    CREATE DATABASE settings_db OWNER settings_svc;
    REVOKE ALL ON DATABASE settings_db FROM PUBLIC;
    GRANT ALL PRIVILEGES ON DATABASE settings_db TO settings_svc;
EOSQL

# 8. audit-log-service (TWO-ROLE SPLIT for immutability enforcement)
echo "Creating audit_migrator (schema owner) and audit_svc (restricted runtime) roles..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE ROLE audit_migrator WITH LOGIN PASSWORD 'audit_migrator_pw';
    CREATE DATABASE audit_db OWNER audit_migrator;
    REVOKE ALL ON DATABASE audit_db FROM PUBLIC;
    GRANT ALL PRIVILEGES ON DATABASE audit_db TO audit_migrator;
    
    CREATE ROLE audit_svc WITH LOGIN PASSWORD 'audit_svc_pw';
    GRANT CONNECT ON DATABASE audit_db TO audit_svc;
EOSQL

echo "Booking-Hub database initialization complete: 8 databases, 9 roles"
