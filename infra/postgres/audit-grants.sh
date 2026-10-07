#!/bin/sh
set -e

# Audit immutability grant enforcement
# Applied AFTER audit-log-service Flyway migration creates audit_log_entries
# Grants audit_svc SELECT/INSERT only, explicitly REVOKES UPDATE/DELETE

echo "Applying audit immutability grants to audit_log_entries..."

psql -h postgres -U audit_migrator -d audit_db <<-EOSQL
    GRANT SELECT, INSERT ON audit_log_entries TO audit_svc;
    REVOKE UPDATE, DELETE ON audit_log_entries FROM audit_svc;
EOSQL

echo "Audit immutability enforced: audit_svc can SELECT/INSERT only"
