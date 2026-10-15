/**
 * exportBookings unit tests
 *
 * Tests cover:
 * - exportToCSV: correct headers, data rows, CSV structure, special character escaping
 * - exportToJSON: correct JSON output with full booking objects
 * - Blob creation with correct mime types, anchor download triggered
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { exportToCSV, exportToJSON } from './exportBookings';
import type { Booking } from '../api/types';

// ── Fixtures ──────────────────────────────────────────────────────────────────

const makeBooking = (overrides: Partial<Booking> = {}): Booking => ({
  id: 'b-1',
  series_id: null,
  location_id: 'loc-1',
  resource_ids: ['res-1', 'res-2'],
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

// ── Test helpers ──────────────────────────────────────────────────────────────

/**
 * Set up browser API mocks and capture blob content.
 * Returns a getter that returns the last blob parts string.
 */
function setupBlobCapture() {
  const capturedParts: { content: string; type: string }[] = [];
  const mockClick = vi.fn();

  // Replace Blob globally with a class-based mock that captures content
  const OriginalBlob = globalThis.Blob;
  class MockBlob {
    content: string;
    type: string;
    constructor(parts: BlobPart[], options?: BlobPropertyBag) {
      this.content = String(parts[0] ?? '');
      this.type = options?.type ?? '';
      capturedParts.push({ content: this.content, type: this.type });
    }
  }
  globalThis.Blob = MockBlob as unknown as typeof Blob;

  const mockCreateObjectURL = vi.fn(() => 'blob:mock-url');
  const mockRevokeObjectURL = vi.fn();
  globalThis.URL.createObjectURL = mockCreateObjectURL;
  globalThis.URL.revokeObjectURL = mockRevokeObjectURL;

  const anchorMock = { href: '', download: '', click: mockClick };
  vi.spyOn(document, 'createElement').mockReturnValue(anchorMock as unknown as HTMLElement);

  return {
    getLastCapture: () => capturedParts[capturedParts.length - 1] ?? null,
    mockClick,
    mockCreateObjectURL,
    mockRevokeObjectURL,
    restore: () => {
      globalThis.Blob = OriginalBlob;
      vi.restoreAllMocks();
      capturedParts.length = 0;
    },
    get lastAnchor() { return anchorMock; },
  };
}

// ── exportToCSV tests ─────────────────────────────────────────────────────────

describe('exportToCSV', () => {
  let ctx: ReturnType<typeof setupBlobCapture>;

  beforeEach(() => { ctx = setupBlobCapture(); });
  afterEach(() => { ctx.restore(); });

  it('triggers a file download click', () => {
    exportToCSV([makeBooking()]);
    expect(ctx.mockClick).toHaveBeenCalledOnce();
  });

  it('sets download filename to "bookings.csv"', () => {
    exportToCSV([makeBooking()]);
    expect(ctx.lastAnchor.download).toBe('bookings.csv');
  });

  it('creates blob with text/csv mime type', () => {
    exportToCSV([makeBooking()]);
    expect(ctx.getLastCapture()?.type).toBe('text/csv');
  });

  it('revokes the object URL after click', () => {
    exportToCSV([makeBooking()]);
    expect(ctx.mockRevokeObjectURL).toHaveBeenCalledWith('blob:mock-url');
  });

  it('includes correct CSV headers as first row', () => {
    exportToCSV([makeBooking()]);
    const lines = ctx.getLastCapture()!.content.split('\n');
    expect(lines[0]).toBe('Title,Location,Resources,Start Time,End Time,Status,Has Conflicts');
  });

  it('produces one data row per booking', () => {
    exportToCSV([makeBooking(), makeBooking({ id: 'b-2', title: 'Board Meeting' })]);
    const lines = ctx.getLastCapture()!.content.split('\n');
    // Header + 2 data rows
    expect(lines).toHaveLength(3);
    expect(lines[1]).toContain('Team Meeting');
    expect(lines[2]).toContain('Board Meeting');
  });

  it('marks "Has Conflicts" as "Yes" for conflicted bookings', () => {
    exportToCSV([
      makeBooking({
        conflict_flags: [{ conflicting_booking_id: 'b-2', conflict_type: 'location' }],
      }),
    ]);
    expect(ctx.getLastCapture()!.content).toContain('Yes');
  });

  it('marks "Has Conflicts" as "No" for non-conflicted bookings', () => {
    exportToCSV([makeBooking({ conflict_flags: [] })]);
    expect(ctx.getLastCapture()!.content).toContain('No');
  });

  it('escapes fields containing commas by wrapping in double-quotes', () => {
    exportToCSV([makeBooking({ title: 'Meeting, Q1' })]);
    expect(ctx.getLastCapture()!.content).toContain('"Meeting, Q1"');
  });

  it('handles empty bookings array — only outputs header row', () => {
    exportToCSV([]);
    const lines = ctx.getLastCapture()!.content.split('\n');
    expect(lines).toHaveLength(1);
    expect(lines[0]).toMatch(/^Title,/);
  });
});

// ── exportToJSON tests ────────────────────────────────────────────────────────

describe('exportToJSON', () => {
  let ctx: ReturnType<typeof setupBlobCapture>;

  beforeEach(() => { ctx = setupBlobCapture(); });
  afterEach(() => { ctx.restore(); });

  it('triggers a file download click', () => {
    exportToJSON([makeBooking()]);
    expect(ctx.mockClick).toHaveBeenCalledOnce();
  });

  it('sets download filename to "bookings.json"', () => {
    exportToJSON([makeBooking()]);
    expect(ctx.lastAnchor.download).toBe('bookings.json');
  });

  it('creates blob with application/json mime type', () => {
    exportToJSON([makeBooking()]);
    expect(ctx.getLastCapture()?.type).toBe('application/json');
  });

  it('produces valid JSON containing all booking fields', () => {
    const booking = makeBooking();
    exportToJSON([booking]);
    const parsed = JSON.parse(ctx.getLastCapture()!.content) as Booking[];
    expect(parsed).toHaveLength(1);
    expect(parsed[0]).toMatchObject({
      id: 'b-1',
      title: 'Team Meeting',
      status: 'approved',
    });
  });

  it('produces pretty-printed JSON with 2-space indentation', () => {
    exportToJSON([makeBooking()]);
    const content = ctx.getLastCapture()!.content;
    // Pretty-printed JSON has newlines and 2-space indentation
    expect(content).toContain('\n');
    expect(content).toContain('  ');
  });

  it('handles empty bookings array — outputs valid empty JSON array', () => {
    exportToJSON([]);
    const parsed = JSON.parse(ctx.getLastCapture()!.content);
    expect(Array.isArray(parsed)).toBe(true);
    expect(parsed).toHaveLength(0);
  });
});
