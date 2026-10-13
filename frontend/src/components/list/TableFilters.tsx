import { useEffect, useState, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import type { Location } from '../../api/types';

export interface TableFiltersState {
  startDate: string;
  endDate: string;
  locationIds: string[];
  statuses: Array<'pending' | 'approved' | 'denied'>;
  query: string;
}

interface TableFiltersProps {
  locations: Location[];
  onChange: (filters: TableFiltersState) => void;
}

function parseLocationIds(param: string | null): string[] {
  if (!param) return [];
  return param.split(',').filter(Boolean);
}

function parseStatuses(param: string | null): Array<'pending' | 'approved' | 'denied'> {
  if (!param) return [];
  const valid = ['pending', 'approved', 'denied'] as const;
  return param
    .split(',')
    .filter((s) => valid.includes(s as 'pending' | 'approved' | 'denied')) as Array<
    'pending' | 'approved' | 'denied'
  >;
}

/**
 * TableFilters — renders date range, location multi-select, status multi-select,
 * and search query input. Syncs to URL query params for bookmarkable URLs (UI-CAL-04).
 */
export function TableFilters({ locations, onChange }: TableFiltersProps) {
  const [searchParams, setSearchParams] = useSearchParams();

  // Initialise filter state from URL params (bookmarkable URL support)
  const [startDate, setStartDate] = useState(searchParams.get('start_date') ?? '');
  const [endDate, setEndDate] = useState(searchParams.get('end_date') ?? '');
  const [locationIds, setLocationIds] = useState<string[]>(
    parseLocationIds(searchParams.get('location_id'))
  );
  const [statuses, setStatuses] = useState<Array<'pending' | 'approved' | 'denied'>>(
    parseStatuses(searchParams.get('status'))
  );
  const [query, setQuery] = useState(searchParams.get('q') ?? '');
  const [debouncedQuery, setDebouncedQuery] = useState(query);

  // Debounce search query 300ms
  useEffect(() => {
    const timer = setTimeout(() => setDebouncedQuery(query), 300);
    return () => clearTimeout(timer);
  }, [query]);

  // Sync filters to URL params and propagate upward
  const syncFilters = useCallback(
    (
      newStart: string,
      newEnd: string,
      newLocationIds: string[],
      newStatuses: Array<'pending' | 'approved' | 'denied'>,
      newQuery: string
    ) => {
      const params: Record<string, string> = {};
      // Preserve existing view param
      const view = searchParams.get('view');
      if (view) params['view'] = view;
      if (newStart) params['start_date'] = newStart;
      if (newEnd) params['end_date'] = newEnd;
      if (newLocationIds.length > 0) params['location_id'] = newLocationIds.join(',');
      if (newStatuses.length > 0) params['status'] = newStatuses.join(',');
      if (newQuery) params['q'] = newQuery;
      setSearchParams(params, { replace: true });
      onChange({ startDate: newStart, endDate: newEnd, locationIds: newLocationIds, statuses: newStatuses, query: newQuery });
    },
    [searchParams, setSearchParams, onChange]
  );

  // When debounced query settles, sync to URL
  useEffect(() => {
    syncFilters(startDate, endDate, locationIds, statuses, debouncedQuery);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedQuery]);

  function handleStartDate(value: string) {
    setStartDate(value);
    syncFilters(value, endDate, locationIds, statuses, debouncedQuery);
  }

  function handleEndDate(value: string) {
    setEndDate(value);
    syncFilters(startDate, value, locationIds, statuses, debouncedQuery);
  }

  function toggleLocation(id: string) {
    const next = locationIds.includes(id)
      ? locationIds.filter((l) => l !== id)
      : [...locationIds, id];
    setLocationIds(next);
    syncFilters(startDate, endDate, next, statuses, debouncedQuery);
  }

  function toggleStatus(status: 'pending' | 'approved' | 'denied') {
    const next = statuses.includes(status)
      ? statuses.filter((s) => s !== status)
      : [...statuses, status];
    setStatuses(next);
    syncFilters(startDate, endDate, locationIds, next, debouncedQuery);
  }

  function handleClear() {
    setStartDate('');
    setEndDate('');
    setLocationIds([]);
    setStatuses([]);
    setQuery('');
    setDebouncedQuery('');
    // Keep the view param, clear everything else
    const view = searchParams.get('view');
    setSearchParams(view ? { view } : {}, { replace: true });
    onChange({ startDate: '', endDate: '', locationIds: [], statuses: [], query: '' });
  }

  const hasFilters =
    startDate || endDate || locationIds.length > 0 || statuses.length > 0 || query;

  return (
    <div className="table-filters" data-testid="table-filters">
      {/* Date range */}
      <div className="table-filters__group">
        <label htmlFor="filter-start-date" className="table-filters__label">
          From
        </label>
        <input
          id="filter-start-date"
          type="date"
          className="table-filters__input"
          value={startDate}
          onChange={(e) => handleStartDate(e.target.value)}
          data-testid="filter-start-date"
        />
      </div>

      <div className="table-filters__group">
        <label htmlFor="filter-end-date" className="table-filters__label">
          To
        </label>
        <input
          id="filter-end-date"
          type="date"
          className="table-filters__input"
          value={endDate}
          onChange={(e) => handleEndDate(e.target.value)}
          data-testid="filter-end-date"
        />
      </div>

      {/* Location multi-select */}
      <div className="table-filters__group table-filters__group--dropdown">
        <span className="table-filters__label">Location</span>
        <div className="table-filters__dropdown" data-testid="filter-location-dropdown">
          <div className="table-filters__dropdown-header">
            {locationIds.length === 0
              ? 'All locations'
              : `${locationIds.length} selected`}
          </div>
          <div className="table-filters__dropdown-body">
            {locations.map((loc) => (
              <label key={loc.id} className="table-filters__check-item">
                <input
                  type="checkbox"
                  checked={locationIds.includes(loc.id)}
                  onChange={() => toggleLocation(loc.id)}
                  data-testid={`filter-location-${loc.id}`}
                />
                <span
                  className="table-filters__location-dot"
                  style={{ backgroundColor: loc.colour }}
                  aria-hidden="true"
                />
                {loc.name}
              </label>
            ))}
          </div>
        </div>
      </div>

      {/* Status multi-select */}
      <div className="table-filters__group" data-testid="filter-status-group">
        <span className="table-filters__label">Status</span>
        <div className="table-filters__status-checks">
          {(['pending', 'approved', 'denied'] as const).map((s) => (
            <label key={s} className="table-filters__check-item">
              <input
                type="checkbox"
                checked={statuses.includes(s)}
                onChange={() => toggleStatus(s)}
                data-testid={`filter-status-${s}`}
              />
              {s.charAt(0).toUpperCase() + s.slice(1)}
            </label>
          ))}
        </div>
      </div>

      {/* Search input */}
      <div className="table-filters__group table-filters__group--search">
        <label htmlFor="filter-search" className="table-filters__label">
          Search
        </label>
        <input
          id="filter-search"
          type="search"
          className="table-filters__input"
          placeholder="Filter by title or description…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          data-testid="filter-search"
        />
      </div>

      {/* Clear button */}
      {hasFilters && (
        <button
          type="button"
          className="table-filters__clear"
          onClick={handleClear}
          data-testid="filter-clear"
        >
          Clear filters
        </button>
      )}
    </div>
  );
}
