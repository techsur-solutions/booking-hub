import { apiClient } from './client';

// ── Types ──────────────────────────────────────────────────────────────────────

/**
 * Entity types surfaced by DomainEventParser (Phase 6 Plan 04).
 * Derived from 7-queue routing keys: booking.*, location.*, resource.*,
 * customfield.*, user.*, permission.*, settings.*
 */
export type AuditLogEntityType =
  | 'booking'
  | 'location'
  | 'resource'
  | 'custom_field'
  | 'user'
  | 'role'
  | 'settings';

export interface AuditLogFilters {
  entity_type?: AuditLogEntityType | '';
  actor?: string;
  start_date?: string; // YYYY-MM-DD
  end_date?: string;   // YYYY-MM-DD
  entity_id?: string;  // UUID — used for deep-linking from Booking Detail Modal
}

/**
 * Audit log entry response shape from GET /audit-log.
 *
 * Matches AuditLogEntryResponse DTO from Phase 6 Plan 04:
 *   AuditLogController → AuditLogSpecifications → audit_log_entries table.
 *
 * NOTE: before_values and after_values are null for all entries until
 * Phase 3/4/5 publishers emit distinct before-snapshots (Phase 6 Plan 04
 * interim default). UI shows "N/A" for these columns per plan spec.
 */
export interface AuditLogEntry {
  id: string;
  entity_type: string;
  entity_id: string;
  actor_id: string;
  action_type: string;
  occurred_at: string; // ISO 8601
  before_values: Record<string, unknown> | null;
  after_values: Record<string, unknown> | null;
  message: string | null;
  data: Record<string, unknown> | null;
  idempotency_key: string | null;
}

// ── API client ─────────────────────────────────────────────────────────────────

export const auditLogApi = {
  /**
   * GET /audit-log — filterable list of audit log entries.
   * Requires role_audit_viewer (enforced by backend Phase 6 Plan 04).
   */
  getFiltered: (params: AuditLogFilters) =>
    apiClient.get<AuditLogEntry[]>('/audit-log', { params }),

  /**
   * GET /audit-log/export — CSV download of filtered results.
   * If backend does not provide this endpoint, client-side CSV generation
   * is used as fallback (see AuditLogViewer.tsx exportCsv()).
   * Blob response type required for download.
   */
  exportCsv: (params: AuditLogFilters) =>
    apiClient.get<Blob>('/audit-log/export', { params, responseType: 'blob' }),
};
