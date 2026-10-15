import { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { AuditLogFilters } from '../../components/admin/AuditLogFilters';
import { AuditLogTable } from '../../components/admin/AuditLogTable';
import { auditLogApi } from '../../api/auditLogApi';
import type { AuditLogFilters as AuditLogFiltersType, AuditLogEntry } from '../../api/auditLogApi';

// ── Helpers ────────────────────────────────────────────────────────────────────

/** Build a timestamped filename for CSV download: "audit-log-2026-10-15.csv" */
function csvFilename(): string {
  const date = new Date().toISOString().slice(0, 10);
  return `audit-log-${date}.csv`;
}

/**
 * Client-side CSV generation fallback.
 * Used when the backend does not provide a /audit-log/export endpoint.
 *
 * Columns: Timestamp, Actor, Entity Type, Entity ID, Action, Message, Data
 */
function generateCsvBlob(entries: AuditLogEntry[]): Blob {
  const escape = (value: string | null | undefined): string => {
    if (value == null) return '';
    const str = String(value).replace(/"/g, '""');
    return `"${str}"`;
  };

  const header = [
    'Timestamp',
    'Actor',
    'Entity Type',
    'Entity ID',
    'Action',
    'Message',
    'Data',
  ].join(',');

  const rows = entries.map((e) =>
    [
      escape(e.occurred_at),
      escape(e.actor_id),
      escape(e.entity_type),
      escape(e.entity_id),
      escape(e.action_type),
      escape(e.message),
      escape(e.data ? JSON.stringify(e.data) : null),
    ].join(',')
  );

  return new Blob([[header, ...rows].join('\n')], { type: 'text/csv' });
}

/** Trigger a browser file download from a Blob */
function downloadBlob(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

// ── Component ──────────────────────────────────────────────────────────────────

/**
 * AuditLogViewer — Admin page for browsing immutable audit log entries.
 *
 * Features:
 * - Filterable table (entity type, actor search, date range)
 * - Export to CSV (backend endpoint or client-side fallback)
 * - Deep-link support: ?entity_type=booking&entity_id=X pre-populates filters
 * - Immutable record notice (no edit/delete buttons)
 *
 * Route: /admin/audit-log
 * Permission: role_audit_viewer (enforced by backend Phase 6 Plan 04; route gated via Sidebar)
 */
export function AuditLogViewer() {
  const [searchParams] = useSearchParams();
  const [entries, setEntries] = useState<AuditLogEntry[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isExporting, setIsExporting] = useState(false);
  const [activeFilters, setActiveFilters] = useState<AuditLogFiltersType>({});

  // ── Deep-link: parse URL params to pre-populate filters ──────────────────────
  // Supports: ?entity_type=booking&entity_id=abc-123 (from Booking Detail Modal)
  const deepLinkFilters: AuditLogFiltersType = {};
  const urlEntityType = searchParams.get('entity_type');
  const urlEntityId = searchParams.get('entity_id');
  if (urlEntityType) {
    deepLinkFilters.entity_type = urlEntityType as AuditLogFiltersType['entity_type'];
  }
  if (urlEntityId) {
    deepLinkFilters.entity_id = urlEntityId;
  }

  // ── Data fetching ────────────────────────────────────────────────────────────

  const fetchEntries = useCallback(async (filters: AuditLogFiltersType) => {
    setIsLoading(true);
    setError(null);
    try {
      // Strip empty-string values so they aren't sent as ?entity_type=
      const cleanParams: AuditLogFiltersType = Object.fromEntries(
        Object.entries(filters).filter(([, v]) => v != null && v !== '')
      );
      const response = await auditLogApi.getFiltered(cleanParams);
      setEntries(response.data);
    } catch (err: unknown) {
      const msg =
        err instanceof Error ? err.message : 'Failed to load audit log entries';
      setError(msg);
      setEntries([]);
    } finally {
      setIsLoading(false);
    }
  }, []);

  // ── Load on mount with deep-link or default filters ──────────────────────────

  useEffect(() => {
    const initialFilters = Object.keys(deepLinkFilters).length > 0 ? deepLinkFilters : {};
    setActiveFilters(initialFilters);
    void fetchEntries(initialFilters);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Mount-only intentional — deep-link params read from URL at mount

  // ── Handlers ─────────────────────────────────────────────────────────────────

  function handleApplyFilters(filters: AuditLogFiltersType) {
    setActiveFilters(filters);
    void fetchEntries(filters);
  }

  async function handleExportCsv() {
    setIsExporting(true);
    try {
      // Clean params same as fetch
      const cleanParams: AuditLogFiltersType = Object.fromEntries(
        Object.entries(activeFilters).filter(([, v]) => v != null && v !== '')
      );

      let blob: Blob;
      try {
        // Try backend CSV export endpoint first
        const response = await auditLogApi.exportCsv(cleanParams);
        blob = response.data;
      } catch {
        // Backend endpoint not available — generate CSV client-side
        blob = generateCsvBlob(entries);
      }
      downloadBlob(blob, csvFilename());
    } catch (err) {
      console.error('[AuditLogViewer] CSV export failed:', err);
    } finally {
      setIsExporting(false);
    }
  }

  // ── Render ───────────────────────────────────────────────────────────────────

  return (
    <div className="audit-log-viewer" data-testid="audit-log-viewer">
      {/* Page header */}
      <div className="audit-log-viewer__header">
        <div className="audit-log-viewer__title-row">
          <h1 className="audit-log-viewer__title">Audit Log</h1>

          {/* Export to CSV button (top-right per UI-ADMIN-07) */}
          <button
            type="button"
            data-testid="btn-export-csv"
            onClick={() => void handleExportCsv()}
            disabled={isExporting || entries.length === 0}
            className="audit-log-viewer__export-btn"
          >
            {isExporting ? 'Exporting…' : 'Export to CSV'}
          </button>
        </div>

        {/* Immutable record notice — positioned near header per UI-ADMIN-07 */}
        {/* read-only indicator signals immutability (T-10-20 mitigated) */}
        <div
          className="audit-log-viewer__immutable-notice read-only"
          data-testid="immutable-notice"
          role="note"
          aria-label="Immutable record indicator"
        >
          <span className="audit-log-viewer__immutable-icon" aria-hidden="true">🔒</span>
          <span className="audit-log-viewer__immutable-text">
            Audit entries are read-only and cannot be edited or deleted.
          </span>
        </div>
      </div>

      {/* Filter controls */}
      <AuditLogFilters
        initialFilters={{ ...deepLinkFilters }}
        onApply={handleApplyFilters}
      />

      {/* Audit log table */}
      <AuditLogTable
        entries={entries}
        isLoading={isLoading}
        error={error}
      />
    </div>
  );
}
