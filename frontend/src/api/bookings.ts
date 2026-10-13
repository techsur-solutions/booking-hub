import { apiClient } from './client';
import type { Booking, ConflictFlag } from './types';

export const bookingsApi = {
  getBookings: (params: {
    start?: string;
    end?: string;
    location_id?: string;
    status?: string;
    q?: string;
  }) => apiClient.get<Booking[]>('/bookings', { params }),

  getBookingById: (id: string) => apiClient.get<Booking>(`/bookings/${id}`),

  createBooking: (
    data: Omit<Booking, 'id' | 'created_at' | 'updated_at' | 'deleted_at'>
  ) => apiClient.post<Booking>('/bookings', data),

  updateBooking: (id: string, data: Partial<Booking>) =>
    apiClient.put<Booking>(`/bookings/${id}`, data),

  deleteBooking: (id: string) => apiClient.delete(`/bookings/${id}`),

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
};
