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

# Verify immutability constraint: audit_svc should NOT have UPDATE privilege
VERIFY_RESULT=$(psql -h postgres -U audit_migrator -d audit_db -t -c \
    "SELECT has_table_privilege('audit_svc', 'audit_log_entries', 'UPDATE');")

if echo "$VERIFY_RESULT" | grep -q "f"; then
    echo "Audit immutability enforced: audit_svc can SELECT/INSERT only"
else
    echo "ERROR: Verification failed - audit_svc still has UPDATE privilege" >&2
    exit 1
fi
