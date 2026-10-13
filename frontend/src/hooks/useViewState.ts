import { useSearchParams } from 'react-router-dom';

// ── Types ─────────────────────────────────────────────────────────────────────

export interface ViewState {
  /** ISO date YYYY-MM-DD */
  date?: string;
  /** Active view mode */
  view?: 'month' | 'day' | 'list';
  /** Location UUID for filtering */
  locationId?: string;
  /** Booking status filter */
  status?: 'pending' | 'approved' | 'denied';
  /** Search query string */
  query?: string;
  /** Start date for List View date range (YYYY-MM-DD) */
  startDate?: string;
  /** End date for List View date range (YYYY-MM-DD) */
  endDate?: string;
}

// ── Validation helpers ────────────────────────────────────────────────────────

/** Validates ISO-8601 date string YYYY-MM-DD */
function isValidIsoDate(s: string): boolean {
  return /^\d{4}-\d{2}-\d{2}$/.test(s);
}

/** Validates a UUID v4 string */
function isValidUuid(s: string): boolean {
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(s);
}

/** Valid view modes */
const VALID_VIEWS = new Set<string>(['month', 'day', 'list']);

/** Valid booking statuses */
const VALID_STATUSES = new Set<string>(['pending', 'approved', 'denied']);

// ── useViewState hook ─────────────────────────────────────────────────────────

/**
 * useViewState
 *
 * Reads and writes view state (date, view mode, location filter, status filter,
 * search query, and date range) to URL query parameters.
 *
 * All values are validated before use — invalid params fall back to defaults
 * (T-09-08-01: URL params are user-controlled and could contain malicious values).
 *
 * Usage:
 *   const { date, view, locationId, updateState } = useViewState();
 *   updateState({ date: '2024-03-01' }); // merges into existing params
 */
export function useViewState() {
  const [searchParams, setSearchParams] = useSearchParams();

  // ── Read state from URL params ──────────────────────────────────────────────

  const rawDate = searchParams.get('date') ?? '';
  const rawView = searchParams.get('view') ?? '';
  const rawLocationId = searchParams.get('location_id') ?? '';
  const rawStatus = searchParams.get('status') ?? '';
  const rawQuery = searchParams.get('q') ?? '';
  const rawStartDate = searchParams.get('start_date') ?? '';
  const rawEndDate = searchParams.get('end_date') ?? '';

  // Validate and sanitise each param — fall back to default when invalid
  const date: string = isValidIsoDate(rawDate)
    ? rawDate
    : new Date().toISOString().split('T')[0];

  const view: ViewState['view'] = VALID_VIEWS.has(rawView)
    ? (rawView as ViewState['view'])
    : 'month';

  const locationId: string | undefined = isValidUuid(rawLocationId)
    ? rawLocationId
    : undefined;

  const status: ViewState['status'] = VALID_STATUSES.has(rawStatus)
    ? (rawStatus as ViewState['status'])
    : undefined;

  // Search query: sanitise by limiting to printable ASCII for safety; allow any non-empty value
  const query: string | undefined = rawQuery.length > 0 ? rawQuery : undefined;

  const startDate: string | undefined = isValidIsoDate(rawStartDate)
    ? rawStartDate
    : undefined;

  const endDate: string | undefined = isValidIsoDate(rawEndDate)
    ? rawEndDate
    : undefined;

  // ── updateState: merge partial updates into existing URL params ────────────

  /**
   * Merges partial ViewState updates into the current URL query params.
   * Keys present in `updates` replace the existing values.
   * Keys set to `undefined` are removed from the URL.
   * Keys not mentioned in `updates` are preserved as-is.
   */
  function updateState(updates: Partial<ViewState>): void {
    const current: Record<string, string> = Object.fromEntries(searchParams.entries());

    // Apply updates, mapping ViewState keys to their URL param names
    if ('date' in updates) {
      if (updates.date !== undefined && isValidIsoDate(updates.date)) {
        current['date'] = updates.date;
      } else if (updates.date === undefined) {
        delete current['date'];
      }
    }

    if ('view' in updates) {
      if (updates.view !== undefined && VALID_VIEWS.has(updates.view)) {
        current['view'] = updates.view;
      } else if (updates.view === undefined) {
        delete current['view'];
      }
    }

    if ('locationId' in updates) {
      if (updates.locationId !== undefined && isValidUuid(updates.locationId)) {
        current['location_id'] = updates.locationId;
      } else {
        delete current['location_id'];
      }
    }

    if ('status' in updates) {
      if (updates.status !== undefined && VALID_STATUSES.has(updates.status)) {
        current['status'] = updates.status;
      } else {
        delete current['status'];
      }
    }

    if ('query' in updates) {
      if (updates.query !== undefined && updates.query.length > 0) {
        current['q'] = updates.query;
      } else {
        delete current['q'];
      }
    }

    if ('startDate' in updates) {
      if (updates.startDate !== undefined && isValidIsoDate(updates.startDate)) {
        current['start_date'] = updates.startDate;
      } else {
        delete current['start_date'];
      }
    }

    if ('endDate' in updates) {
      if (updates.endDate !== undefined && isValidIsoDate(updates.endDate)) {
        current['end_date'] = updates.endDate;
      } else {
        delete current['end_date'];
      }
    }

    setSearchParams(current);
  }

  return {
    date,
    view,
    locationId,
    status,
    query,
    startDate,
    endDate,
    updateState,
  };
}
