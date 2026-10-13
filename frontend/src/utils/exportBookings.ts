import type { Booking } from '../api/types';

/**
 * Download a string as a file in the user's browser.
 * Creates a temporary anchor element, sets href to a Blob URL, clicks it,
 * then immediately revokes the URL.
 */
function downloadFile(content: string, filename: string, mimeType: string): void {
  const blob = new Blob([content], { type: mimeType });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}

/**
 * Export an array of bookings as a CSV file (bookings.csv).
 *
 * Columns: Title, Location, Resources, Start Time, End Time, Status, Has Conflicts
 *
 * Only the bookings currently visible in the table (post-filter) are exported.
 */
export function exportToCSV(bookings: Booking[]): void {
  const headers = [
    'Title',
    'Location',
    'Resources',
    'Start Time',
    'End Time',
    'Status',
    'Has Conflicts',
  ];

  const rows = bookings.map((b) => [
    b.title,
    b.location_id,
    (b.resource_ids ?? []).join('; '),
    b.start_time,
    b.end_time,
    b.status,
    (b.conflict_flags ?? []).length > 0 ? 'Yes' : 'No',
  ]);

  // Escape CSV values: wrap in double-quotes if value contains comma, quote, or newline
  function escapeCSV(value: string): string {
    if (value.includes(',') || value.includes('"') || value.includes('\n')) {
      return `"${value.replace(/"/g, '""')}"`;
    }
    return value;
  }

  const csvLines = [headers, ...rows].map((row) =>
    row.map((cell) => escapeCSV(String(cell ?? ''))).join(',')
  );

  const csv = csvLines.join('\n');
  downloadFile(csv, 'bookings.csv', 'text/csv');
}

/**
 * Export an array of bookings as a JSON file (bookings.json).
 *
 * Produces the full booking objects with 2-space indentation.
 * Only the bookings currently visible in the table (post-filter) are exported.
 */
export function exportToJSON(bookings: Booking[]): void {
  const json = JSON.stringify(bookings, null, 2);
  downloadFile(json, 'bookings.json', 'application/json');
}
