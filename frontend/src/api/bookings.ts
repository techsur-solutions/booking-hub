import { apiClient } from './client';
import type { Booking, ConflictFlag } from './types';
import type { RecurringScope } from '../components/booking/RecurringScopeDialog';

/** Response shape for PATCH /bookings/:id */
export interface BookingUpdateResponse extends Booking {
  /** Number of occurrences updated (only for entire-series scope) */
  recurrence_updated_count?: number;
}

export const bookingsApi = {
  getBookings: (params: {
    start?: string;
    end?: string;
    location_id?: string;
    status?: string;
    q?: string;
  }) => apiClient.get<Booking[]>('/bookings', { params }),

  getBookingById: (id: string) => apiClient.get<Booking>(`/bookings/${id}`),

  /** Alias required by UI-BOOK-03 integration contract */
  getById: (id: string) => apiClient.get<Booking>(`/bookings/${id}`),

  createBooking: (
    data: Omit<Booking, 'id' | 'created_at' | 'updated_at' | 'deleted_at'>
  ) => apiClient.post<Booking>('/bookings', data),

  updateBooking: (id: string, data: Partial<Booking>) =>
    apiClient.put<Booking>(`/bookings/${id}`, data),

  deleteBooking: (id: string) => apiClient.delete(`/bookings/${id}`),

  /** Delete booking with optional scope for recurring series */
  deleteBookingWithScope: (id: string, scope: 'this-only' | 'entire-series') =>
    apiClient.delete(`/bookings/${id}?scope=${scope}`),

  checkConflicts: (data: {
    location_id: string;
    resource_ids: string[];
    start_time: string;
    end_time: string;
    exclude_booking_id?: string;
  }) => apiClient.post<ConflictFlag[]>('/bookings/check-conflicts', data),

  approveBooking: (id: string) =>
    apiClient.post(`/bookings/${id}/approve`),

  denyBooking: (id: string, reason?: string) =>
    apiClient.post(`/bookings/${id}/deny`, { reason }),

  cloneBooking: (id: string) =>
    apiClient.post<Booking>(`/bookings/${id}/clone`),

  /**
   * PATCH /bookings/:id?scope={scope}
   * Updates a booking. For recurring bookings, scope determines whether
   * only this occurrence or the entire series is updated.
   *
   * From TechArch §4.2.3:
   *   PATCH /bookings/:id?scope=this-only|entire-series
   */
  patchBooking: (
    id: string,
    data: Record<string, unknown>,
    scope?: RecurringScope
  ) => {
    const url = scope ? `/bookings/${id}?scope=${scope}` : `/bookings/${id}`;
    return apiClient.patch<BookingUpdateResponse>(url, data);
  },
};
