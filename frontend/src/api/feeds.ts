import { apiClient } from './client';
import type { FeedBooking } from './types';

export const feedsApi = {
  getFeedBookings: (params?: { location_id?: string; start?: string; end?: string }) =>
    apiClient.get<FeedBooking[]>('/feeds/bookings', { params }),

  getJsonFeed: (apiToken: string, params?: { location_id?: string }) =>
    apiClient.get<FeedBooking[]>('/feeds/json', {
      params: { ...params, api_token: apiToken },
    }),

  getICalFeedUrl: (apiToken: string) =>
    `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/feeds/ical?api_token=${apiToken}`,

  getRssFeedUrl: (apiToken: string) =>
    `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/feeds/rss?api_token=${apiToken}`,
};
