import { useState, useCallback } from 'react';
import { useBookings } from '../hooks/useBookings';
import { useLocations } from '../hooks/useLocations';
import { useResources } from '../hooks/useResources';
import { useViewState } from '../hooks/useViewState';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { BookingTable } from '../components/list/BookingTable';
import { TableFilters } from '../components/list/TableFilters';
import { BookingDetailModal } from '../components/booking/BookingDetailModal';
import type { TableFiltersState } from '../components/list/TableFilters';
import type { Booking } from '../api/types';
import { exportToCSV, exportToJSON } from '../utils/exportBookings';

// ── ListView Page ─────────────────────────────────────────────────────────────

/**
 * ListView
 *
 * Filterable, sortable table view of bookings with:
 * - Date range, location multi-select, status multi-select, and search filters
 * - URL query param sync for bookmarkable filter state (UI-CAL-04)
 * - ConflictBadge rendering in Conflict Flags column
 * - Row click opens Booking Detail Modal
 * - CSV/JSON export of currently filtered bookings
 */
export function ListView() {
  // Read view state from URL (UI-CAL-04): TableFilters also syncs to URL,
  // but useViewState ensures ListView itself reads the correct initial state
  // when navigating to a bookmarked URL.
  const { startDate: urlStartDate, endDate: urlEndDate, locationId: urlLocationId, status: urlStatus, query: urlQuery } = useViewState();

  const [detailBookingId, setDetailBookingId] = useState<string | null>(null);
  const [filters, setFilters] = useState<TableFiltersState>({
    startDate: urlStartDate ?? '',
    endDate: urlEndDate ?? '',
    // TableFilters uses locationIds[] (multi-select); seed with single locationId from URL if present
    locationIds: urlLocationId ? [urlLocationId] : [],
    statuses: urlStatus ? [urlStatus] : [],
    query: urlQuery ?? '',
  });

  // Fetch data
  const { data: locations = [] } = useLocations();
  const { data: resources = [] } = useResources();

  // Build API params from filter state
  const apiParams: Parameters<typeof useBookings>[0] = {};
  if (filters.startDate) apiParams.start = `${filters.startDate}T00:00:00`;
  if (filters.endDate) apiParams.end = `${filters.endDate}T23:59:59`;
  // API supports single location_id filter; for multi-select, do client-side post-filter
  if (filters.statuses.length === 1) apiParams.status = filters.statuses[0];
  if (filters.query) apiParams.q = filters.query;

  const { data: rawBookings = [], isLoading, isError } = useBookings(apiParams);

  // Client-side post-filter for location (multi-select) and status (multi-select).
  // Status is also sent to the API as a hint for 1-item selections, but we always
  // apply it client-side too to ensure correctness when the API returns unfiltered data
  // (e.g., in tests or when the backend ignores the hint).
  const filteredBookings = rawBookings.filter((b) => {
    if (filters.locationIds.length > 0 && !filters.locationIds.includes(b.location_id)) {
      return false;
    }
    if (filters.statuses.length > 0 && !filters.statuses.includes(b.status)) {
      return false;
    }
    return true;
  });

  const handleFiltersChange = useCallback((next: TableFiltersState) => {
    setFilters(next);
  }, []);

  function handleRowClick(booking: Booking) {
    setDetailBookingId(booking.id);
  }

  function handleExportCSV() {
    exportToCSV(filteredBookings);
  }

  function handleExportJSON() {
    exportToJSON(filteredBookings);
  }

  return (
    <div className="list-view" data-testid="list-view">
      {/* ── Header ── */}
      <div className="list-view__header">
        <h1 className="list-view__title">List View</h1>

        <div className="list-view__export-buttons">
          <button
            type="button"
            className="btn btn--secondary btn--small"
            onClick={handleExportCSV}
            data-testid="export-csv"
            disabled={filteredBookings.length === 0}
            aria-label={`Export ${filteredBookings.length} booking(s) to CSV`}
          >
            Export CSV
          </button>
          <button
            type="button"
            className="btn btn--secondary btn--small"
            onClick={handleExportJSON}
            data-testid="export-json"
            disabled={filteredBookings.length === 0}
            aria-label={`Export ${filteredBookings.length} booking(s) to JSON`}
          >
            Export JSON
          </button>
        </div>
      </div>

      {/* ── Filters ── */}
      <TableFilters locations={locations} onChange={handleFiltersChange} />

      {/* ── Table ── */}
      {isLoading ? (
        <div className="list-view__loading">
          <LoadingSpinner />
        </div>
      ) : isError ? (
        <div className="list-view__error" role="alert">
          Failed to load bookings. Please try again.
        </div>
      ) : (
        <>
          <div className="list-view__count" aria-live="polite" aria-atomic="true">
            {filteredBookings.length === 1
              ? '1 booking'
              : `${filteredBookings.length} bookings`}
          </div>
          <BookingTable
            bookings={filteredBookings}
            locations={locations}
            resources={resources}
            onRowClick={handleRowClick}
          />
        </>
      )}

      {/* ── Detail Modal ── */}
      <BookingDetailModal
        bookingId={detailBookingId}
        onClose={() => setDetailBookingId(null)}
      />
    </div>
  );
}
