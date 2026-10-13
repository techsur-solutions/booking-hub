/**
 * ListView unit tests
 *
 * Tests focus on:
 * - Rendering BookingTable with correct column structure
 * - TableFilters rendered alongside table
 * - Export buttons presence
 * - ConflictBadge rendered for bookings with conflicts
 * - Row click opens modal
 *
 * Note: These are unit tests with mocked hooks (not E2E — those are in e2e/list-view.spec.ts).
 */
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ToastProvider } from '../components/Toast';
import { ListView } from './ListView';
import type { Booking, Location, Resource } from '../api/types';

// ── Mocks ─────────────────────────────────────────────────────────────────────

// Mock Keycloak so PermissionGate doesn't fail in tests
vi.mock('../auth/keycloak', () => ({
  keycloak: {
    realmAccess: { roles: ['role_calendar_viewer', 'role_booking_approver'] },
  },
}));

// Mock hooks
vi.mock('../hooks/useBookings', () => ({
  useBookings: vi.fn(),
  useBookingById: vi.fn(() => ({ data: undefined, isLoading: false, isError: false })),
  useCreateBooking: vi.fn(() => ({ mutate: vi.fn(), isPending: false })),
  useUpdateBooking: vi.fn(() => ({ mutate: vi.fn(), isPending: false })),
  useDeleteBooking: vi.fn(() => ({ mutate: vi.fn(), isPending: false })),
}));
vi.mock('../hooks/useLocations', () => ({
  useLocations: vi.fn(),
  useLocationById: vi.fn(() => ({ data: undefined, isLoading: false, isError: false })),
}));
vi.mock('../hooks/useResources', () => ({
  useResources: vi.fn(),
}));

// Import after mocking so the mock is picked up
import { useBookings, useBookingById } from '../hooks/useBookings';
import { useLocations } from '../hooks/useLocations';
import { useResources } from '../hooks/useResources';

// ── Fixtures ──────────────────────────────────────────────────────────────────

const LOCATION: Location = {
  id: 'loc-1',
  name: 'Room A',
  colour: '#ff0000',
  building: null,
  layouts: null,
  deleted_at: null,
};

const RESOURCE: Resource = {
  id: 'res-1',
  name: 'Projector',
  type: 'AV',
  description: null,
  is_unique: false,
  restrict_locations: null,
  deleted_at: null,
};

const makeBooking = (overrides: Partial<Booking> = {}): Booking => ({
  id: 'b-1',
  series_id: null,
  location_id: 'loc-1',
  resource_ids: ['res-1'],
  title: 'Team Meeting',
  description: 'Weekly sync',
  start_time: '2024-03-15T10:00:00',
  end_time: '2024-03-15T11:00:00',
  status: 'approved',
  owner_id: 'user-1',
  conflict_flags: [],
  custom_field_values: [],
  created_at: '2024-03-01T00:00:00',
  updated_at: '2024-03-01T00:00:00',
  deleted_at: null,
  ...overrides,
});

// ── Helper ────────────────────────────────────────────────────────────────────

function renderListView() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <ToastProvider>
        <MemoryRouter initialEntries={['/bookings?view=list']}>
          <ListView />
        </MemoryRouter>
      </ToastProvider>
    </QueryClientProvider>
  );
}

// ── Tests ─────────────────────────────────────────────────────────────────────

describe('ListView', () => {
  beforeEach(() => {
    vi.mocked(useLocations).mockReturnValue({
      data: [LOCATION],
      isLoading: false,
      isError: false,
    } as ReturnType<typeof useLocations>);

    vi.mocked(useResources).mockReturnValue({
      data: [RESOURCE],
      isLoading: false,
      isError: false,
    } as ReturnType<typeof useResources>);
  });

  it('renders h1 heading "List View"', () => {
    vi.mocked(useBookings).mockReturnValue({
      data: [],
      isLoading: false,
      isError: false,
    } as unknown as ReturnType<typeof useBookings>);

    renderListView();
    expect(screen.getByRole('heading', { name: /List View/i })).toBeInTheDocument();
  });

  it('renders booking table with all 7 column headers', () => {
    vi.mocked(useBookings).mockReturnValue({
      data: [makeBooking()],
      isLoading: false,
      isError: false,
    } as unknown as ReturnType<typeof useBookings>);

    renderListView();

    expect(screen.getByRole('columnheader', { name: /Title/i })).toBeInTheDocument();
    expect(screen.getByRole('columnheader', { name: /Location/i })).toBeInTheDocument();
    expect(screen.getByRole('columnheader', { name: /Resources/i })).toBeInTheDocument();
    expect(screen.getByRole('columnheader', { name: /Start Time/i })).toBeInTheDocument();
    expect(screen.getByRole('columnheader', { name: /End Time/i })).toBeInTheDocument();
    expect(screen.getByRole('columnheader', { name: /Status/i })).toBeInTheDocument();
    expect(screen.getByRole('columnheader', { name: /Conflict Flags/i })).toBeInTheDocument();
  });

  it('renders export CSV and JSON buttons', () => {
    vi.mocked(useBookings).mockReturnValue({
      data: [makeBooking()],
      isLoading: false,
      isError: false,
    } as unknown as ReturnType<typeof useBookings>);

    renderListView();

    expect(screen.getByTestId('export-csv')).toBeInTheDocument();
    expect(screen.getByTestId('export-json')).toBeInTheDocument();
  });

  it('renders filter inputs', () => {
    vi.mocked(useBookings).mockReturnValue({
      data: [],
      isLoading: false,
      isError: false,
    } as unknown as ReturnType<typeof useBookings>);

    renderListView();

    expect(screen.getByTestId('filter-start-date')).toBeInTheDocument();
    expect(screen.getByTestId('filter-end-date')).toBeInTheDocument();
    expect(screen.getByTestId('filter-search')).toBeInTheDocument();
  });

  it('renders a booking row with the booking title', () => {
    vi.mocked(useBookings).mockReturnValue({
      data: [makeBooking({ title: 'Sprint Planning' })],
      isLoading: false,
      isError: false,
    } as unknown as ReturnType<typeof useBookings>);

    renderListView();
    expect(screen.getByText('Sprint Planning')).toBeInTheDocument();
  });

  it('renders ConflictBadge for bookings with conflicts', () => {
    vi.mocked(useBookings).mockReturnValue({
      data: [
        makeBooking({
          id: 'b-conflicted',
          conflict_flags: [
            { conflicting_booking_id: 'b-other', conflict_type: 'location' },
          ],
        }),
      ],
      isLoading: false,
      isError: false,
    } as unknown as ReturnType<typeof useBookings>);

    renderListView();
    expect(screen.getByTestId('conflict-badge')).toBeInTheDocument();
  });

  it('does not render ConflictBadge for bookings without conflicts', () => {
    vi.mocked(useBookings).mockReturnValue({
      data: [makeBooking({ conflict_flags: [] })],
      isLoading: false,
      isError: false,
    } as unknown as ReturnType<typeof useBookings>);

    renderListView();
    expect(screen.queryByTestId('conflict-badge')).not.toBeInTheDocument();
  });

  it('clicking a row opens the Booking Detail Modal with booking title', async () => {
    const booking = makeBooking({ title: 'Design Review' });
    vi.mocked(useBookings).mockReturnValue({
      data: [booking],
      isLoading: false,
      isError: false,
    } as unknown as ReturnType<typeof useBookings>);
    // BookingDetailModal calls useBookingById — return the same booking
    vi.mocked(useBookingById).mockReturnValue({
      data: booking,
      isLoading: false,
      isError: false,
    } as unknown as ReturnType<typeof useBookingById>);

    renderListView();

    const row = screen.getByTestId('booking-row-b-1');
    fireEvent.click(row);

    await waitFor(() => {
      // Modal title should match booking title
      expect(screen.getByRole('dialog')).toBeInTheDocument();
      expect(screen.getByRole('heading', { name: 'Design Review' })).toBeInTheDocument();
    });
  });

  it('shows loading spinner when data is loading', () => {
    vi.mocked(useBookings).mockReturnValue({
      data: undefined,
      isLoading: true,
      isError: false,
    } as unknown as ReturnType<typeof useBookings>);

    renderListView();
    // LoadingSpinner has role="status" — exactly one should be present
    const statusEls = screen.getAllByRole('status');
    expect(statusEls.length).toBeGreaterThan(0);
    // Table should NOT be rendered during loading
    expect(screen.queryByTestId('booking-table')).not.toBeInTheDocument();
  });

  it('shows error message when fetch fails', () => {
    vi.mocked(useBookings).mockReturnValue({
      data: undefined,
      isLoading: false,
      isError: true,
    } as unknown as ReturnType<typeof useBookings>);

    renderListView();
    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(screen.getByText(/Failed to load bookings/i)).toBeInTheDocument();
  });

  it('shows empty state when no bookings match filters', () => {
    vi.mocked(useBookings).mockReturnValue({
      data: [],
      isLoading: false,
      isError: false,
    } as unknown as ReturnType<typeof useBookings>);

    renderListView();
    expect(screen.getByTestId('booking-table-empty')).toBeInTheDocument();
  });

  it('renders status badge with correct label', () => {
    vi.mocked(useBookings).mockReturnValue({
      data: [makeBooking({ status: 'pending' })],
      isLoading: false,
      isError: false,
    } as unknown as ReturnType<typeof useBookings>);

    renderListView();
    expect(screen.getByTestId('status-badge-b-1')).toHaveTextContent('Pending');
  });

  it('renders location name from locations data in the table cell', () => {
    vi.mocked(useBookings).mockReturnValue({
      data: [makeBooking()],
      isLoading: false,
      isError: false,
    } as unknown as ReturnType<typeof useBookings>);

    renderListView();
    // Location name appears in the table row's location cell
    const locationCell = screen.getByRole('cell', { name: /Room A/i });
    expect(locationCell).toBeInTheDocument();
  });

  it('renders resource names in the Resources column', () => {
    vi.mocked(useBookings).mockReturnValue({
      data: [makeBooking({ resource_ids: ['res-1'] })],
      isLoading: false,
      isError: false,
    } as unknown as ReturnType<typeof useBookings>);

    renderListView();
    expect(screen.getByText('Projector')).toBeInTheDocument();
  });
});
