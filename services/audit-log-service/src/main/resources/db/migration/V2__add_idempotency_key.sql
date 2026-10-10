-- Y3-integrations.md mandates idempotent consumption for BOTH F8 and F11, but V1's DDL only
-- gave notification_deliveries an idempotency_key column. This additive migration closes that
-- gap for audit_log_entries. Nullable + partial unique index (not NOT NULL) because this is an
-- additive change to a table whose V1 definition had no such column — going forward every
-- consumer-written row populates it, making it effectively required in practice without a
-- blocking NOT NULL constraint that could reject a hypothetical pre-existing row.
--
-- Applied by audit_migrator schema-owner role (configured as Flyway's user in application.yml),
-- distinct from the audit_svc runtime role targeted by V1's REVOKE UPDATE, DELETE FROM PUBLIC
-- grant. ALTER TABLE ADD COLUMN is a DDL/schema-owner operation (not a row-level UPDATE/DELETE),
-- so the V1 immutability grant is completely untouched by this migration.
ALTER TABLE audit_log_entries ADD COLUMN idempotency_key UUID NULL;

CREATE UNIQUE INDEX uq_audit_log_entries_idempotency_key
    ON audit_log_entries (idempotency_key)
    WHERE idempotency_key IS NOT NULL;
