import { useState, useEffect } from 'react';
import type { AuditLogEntityType, AuditLogFilters } from '../../api/auditLogApi';

// ── Types ──────────────────────────────────────────────────────────────────────

interface AuditLogFiltersProps {
  /** Initial filter values (populated from URL deep-link params) */
  initialFilters?: AuditLogFilters;
  /** Callback invoked when user clicks "Apply Filters" */
  onApply: (filters: AuditLogFilters) => void;
}

// ── Helpers ────────────────────────────────────────────────────────────────────

/** Calculate date string for N days ago (YYYY-MM-DD) */
function daysAgo(n: number): string {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return d.toISOString().slice(0, 10);
}

/** Today as YYYY-MM-DD */
function today(): string {
  return new Date().toISOString().slice(0, 10);
}

function defaultFilters(): AuditLogFilters {
  return {
    entity_type: '',
    actor: '',
    start_date: daysAgo(30),
    end_date: today(),
  };
}

const ENTITY_TYPES: Array<{ value: AuditLogEntityType | ''; label: string }> = [
  { value: '', label: 'All' },
  { value: 'booking', label: 'Booking' },
  { value: 'location', label: 'Location' },
  { value: 'resource', label: 'Resource' },
  { value: 'custom_field', label: 'Custom Field' },
  { value: 'user', label: 'User' },
  { value: 'role', label: 'Role' },
  { value: 'settings', label: 'Settings' },
];

// ── Component ──────────────────────────────────────────────────────────────────

export function AuditLogFilters({ initialFilters, onApply }: AuditLogFiltersProps) {
  const [filters, setFilters] = useState<AuditLogFilters>(() => ({
    ...defaultFilters(),
    ...initialFilters,
  }));

  // Sync if initialFilters change (e.g., URL deep-link param update)
  useEffect(() => {
    if (initialFilters) {
      setFilters((prev) => ({ ...defaultFilters(), ...prev, ...initialFilters }));
    }
  }, [initialFilters?.entity_type, initialFilters?.entity_id]);

  function handleChange(field: keyof AuditLogFilters, value: string) {
    setFilters((prev) => ({ ...prev, [field]: value }));
  }

  function handleApply() {
    onApply(filters);
  }

  function handleClear() {
    const cleared = defaultFilters();
    setFilters(cleared);
    onApply(cleared);
  }

  return (
    <div className="audit-log-filters" data-testid="audit-log-filters">
      {/* Entity Type dropdown */}
      <div className="audit-log-filters__field">
        <label htmlFor="filter-entity-type" className="audit-log-filters__label">
          Entity Type
        </label>
        <select
          id="filter-entity-type"
          data-testid="filter-entity-type"
          value={filters.entity_type ?? ''}
          onChange={(e) => handleChange('entity_type', e.target.value)}
          className="audit-log-filters__select"
        >
          {ENTITY_TYPES.map(({ value, label }) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </select>
      </div>

      {/* Actor search */}
      <div className="audit-log-filters__field">
        <label htmlFor="filter-actor" className="audit-log-filters__label">
          Actor
        </label>
        <input
          id="filter-actor"
          type="text"
          data-testid="filter-actor"
          value={filters.actor ?? ''}
          onChange={(e) => handleChange('actor', e.target.value)}
          placeholder="Search by actor name or email"
          className="audit-log-filters__input"
        />
      </div>

      {/* Start Date */}
      <div className="audit-log-filters__field">
        <label htmlFor="filter-start-date" className="audit-log-filters__label">
          Start Date
        </label>
        <input
          id="filter-start-date"
          type="date"
          data-testid="filter-start-date"
          value={filters.start_date ?? ''}
          onChange={(e) => handleChange('start_date', e.target.value)}
          className="audit-log-filters__input"
        />
      </div>

      {/* End Date */}
      <div className="audit-log-filters__field">
        <label htmlFor="filter-end-date" className="audit-log-filters__label">
          End Date
        </label>
        <input
          id="filter-end-date"
          type="date"
          data-testid="filter-end-date"
          value={filters.end_date ?? ''}
          onChange={(e) => handleChange('end_date', e.target.value)}
          className="audit-log-filters__input"
        />
      </div>

      {/* Actions */}
      <div className="audit-log-filters__actions">
        <button
          type="button"
          data-testid="btn-apply-filters"
          onClick={handleApply}
          className="audit-log-filters__btn audit-log-filters__btn--primary"
        >
          Apply Filters
        </button>
        <button
          type="button"
          data-testid="btn-clear-filters"
          onClick={handleClear}
          className="audit-log-filters__btn audit-log-filters__btn--secondary"
        >
          Clear Filters
        </button>
      </div>
    </div>
  );
}
