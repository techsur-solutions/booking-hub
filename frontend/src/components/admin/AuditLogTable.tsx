import { useState } from 'react';
import type { AuditLogEntry } from '../../api/auditLogApi';

// ── Types ──────────────────────────────────────────────────────────────────────

interface AuditLogTableProps {
  entries: AuditLogEntry[];
  isLoading?: boolean;
  error?: string | null;
}

// ── Helpers ────────────────────────────────────────────────────────────────────

/** Format ISO timestamp to "YYYY-MM-DD HH:mm:ss" */
function formatTimestamp(iso: string): string {
  try {
    const d = new Date(iso);
    const pad = (n: number) => String(n).padStart(2, '0');
    return (
      `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ` +
      `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    );
  } catch {
    return iso;
  }
}

/** Truncate UUID for display; show first 8 chars + '…' */
function truncateId(id: string): string {
  if (!id) return 'N/A';
  if (id.length <= 12) return id;
  return `${id.slice(0, 8)}…`;
}

/** Humanise action_type: "booking.created" → "created" */
function formatAction(actionType: string): string {
  if (!actionType) return 'N/A';
  const parts = actionType.split('.');
  return parts[parts.length - 1] ?? actionType;
}

// ── Sub-component: Entity ID cell with tooltip ─────────────────────────────────

function EntityIdCell({ entityId }: { entityId: string }) {
  const [showTooltip, setShowTooltip] = useState(false);

  return (
    <span
      className="audit-table__entity-id"
      title={entityId}
      onMouseEnter={() => setShowTooltip(true)}
      onMouseLeave={() => setShowTooltip(false)}
      data-testid={`entity-id-${entityId}`}
    >
      {truncateId(entityId)}
      {showTooltip && (
        <span className="audit-table__tooltip" role="tooltip">
          {entityId}
        </span>
      )}
    </span>
  );
}

// ── Main component ─────────────────────────────────────────────────────────────

/**
 * AuditLogTable — read-only table of audit log entries.
 *
 * Per UI-ADMIN-07 requirements:
 * - Columns: Timestamp, Actor, Entity Type, Entity ID, Action, Before/After
 * - NO edit or delete action buttons on rows (immutability principle)
 * - Before/After shows "N/A" (Phase 6 Plan 04 interim: no before-snapshot in publishers)
 * - Entity ID truncated with title-tooltip for full UUID
 */
export function AuditLogTable({ entries, isLoading, error }: AuditLogTableProps) {
  if (isLoading) {
    return (
      <div className="audit-table__state" data-testid="audit-table-loading">
        <span>Loading audit log entries…</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="audit-table__state audit-table__state--error" data-testid="audit-table-error">
        <span>Error: {error}</span>
      </div>
    );
  }

  if (entries.length === 0) {
    return (
      <div className="audit-table__state" data-testid="audit-table-empty">
        <span>No audit log entries found for the selected filters.</span>
      </div>
    );
  }

  return (
    <div className="audit-table-wrapper" data-testid="audit-log-table">
      {/* Accessibility: announce to screen readers this is a read-only immutable table */}
      <table
        className="audit-table"
        aria-label="Audit log entries — read-only"
        data-testid="audit-table"
      >
        <thead>
          <tr>
            <th scope="col" className="audit-table__th">Timestamp</th>
            <th scope="col" className="audit-table__th">Actor</th>
            <th scope="col" className="audit-table__th">Entity Type</th>
            <th scope="col" className="audit-table__th">Entity ID</th>
            <th scope="col" className="audit-table__th">Action</th>
            <th scope="col" className="audit-table__th">Before</th>
            <th scope="col" className="audit-table__th">After</th>
          </tr>
        </thead>
        <tbody>
          {entries.map((entry) => (
            <tr
              key={entry.id}
              className="audit-table__row"
              data-testid={`audit-row-${entry.id}`}
            >
              {/* Timestamp — sortable via server-side; display only here */}
              <td className="audit-table__td audit-table__td--timestamp">
                <time dateTime={entry.occurred_at}>
                  {formatTimestamp(entry.occurred_at)}
                </time>
              </td>

              {/* Actor — actor_id (UUID from event payload; display as-is until user lookup added) */}
              <td className="audit-table__td audit-table__td--actor" data-testid={`actor-${entry.id}`}>
                {entry.actor_id || 'Unknown'}
              </td>

              {/* Entity Type */}
              <td className="audit-table__td audit-table__td--entity-type" data-testid={`entity-type-${entry.id}`}>
                <span className="audit-table__badge">
                  {entry.entity_type || 'N/A'}
                </span>
              </td>

              {/* Entity ID — truncated with tooltip for full UUID */}
              <td className="audit-table__td audit-table__td--entity-id">
                <EntityIdCell entityId={entry.entity_id} />
              </td>

              {/* Action */}
              <td className="audit-table__td audit-table__td--action" data-testid={`action-${entry.id}`}>
                <span className="audit-table__action">
                  {formatAction(entry.action_type)}
                </span>
              </td>

              {/* Before values — Phase 6 Plan 04: no before-snapshot yet; show N/A */}
              <td className="audit-table__td audit-table__td--before">
                {entry.before_values != null ? (
                  <pre className="audit-table__json">
                    {JSON.stringify(entry.before_values, null, 2)}
                  </pre>
                ) : (
                  <span className="audit-table__na">N/A</span>
                )}
              </td>

              {/* After values — shows raw JSONB from after_values column (Phase 6 Plan 04) */}
              <td className="audit-table__td audit-table__td--after">
                {entry.after_values != null ? (
                  <pre className="audit-table__json">
                    {JSON.stringify(entry.after_values, null, 2)}
                  </pre>
                ) : (
                  <span className="audit-table__na">N/A</span>
                )}
              </td>

              {/*
               * NO edit or delete buttons here.
               * Immutability enforced at:
               *   1. DB level: REVOKE UPDATE/DELETE on audit_log_entries (Phase 6 Plan 03)
               *   2. API level: GET-only surface, no PUT/DELETE/POST (Phase 6 Plan 04)
               *   3. UI level: No action buttons on this table (UI-ADMIN-07 / T-10-20)
               */}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
