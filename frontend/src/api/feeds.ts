import { apiClient } from './client';
import type { FeedBooking } from './types';
import { absoluteApiUrl } from './baseUrl';

export const feedsApi = {
  /** Fetch approved bookings for the Display Board kiosk (public, no auth required). */
  getDisplayBoard: (params?: { location_id?: string }) =>
    apiClient.get<FeedBooking[]>('/feeds/display-board', { params }),

  getFeedBookings: (params?: { location_id?: string; start?: string; end?: string }) =>
    apiClient.get<FeedBooking[]>('/feeds/bookings', { params }),

  getJsonFeed: (params?: { location_id?: string }) =>
    apiClient.get<FeedBooking[]>('/feeds/json', { params }),

  getJsonFeedUrl: (params?: { location_id?: string }) => {
    const url = absoluteApiUrl('/feeds/json');
    if (params?.location_id) url.searchParams.set('location_id', params.location_id);
    return url.toString();
  },

  getICalFeedUrl: (params?: { location_id?: string }) => {
    const url = absoluteApiUrl('/feeds/ical');
    if (params?.location_id) url.searchParams.set('location_id', params.location_id);
    return url.toString();
  },

  getRss2FeedUrl: (params?: { location_id?: string }) => {
    const url = absoluteApiUrl('/feeds/rss2');
    if (params?.location_id) url.searchParams.set('location_id', params.location_id);
    return url.toString();
  },

  /** @deprecated Use getRss2FeedUrl. Kept for backward compat. */
  getRssFeedUrl: (apiToken: string) =>
    absoluteApiUrl(`/feeds/rss?api_token=${apiToken}`).toString(),
};
