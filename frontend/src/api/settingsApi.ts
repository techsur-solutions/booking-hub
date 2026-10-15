import { apiClient } from './client';
import type { Settings } from './types';

// Re-export Settings from the canonical types module so all consumers import from one place.
export type { Settings };

export interface SettingsUpdateRequest {
  approve_booking: boolean;
  calendar_slot_size: number;
  calendar_min_time: string;
  calendar_max_time: string;
}

/**
 * Settings API client methods.
 *
 * GET  /settings/current — reads DB directly on every call (no in-memory cache per
 *   Phase 4 Plan 06 decision: "No in-memory settings cache at all — getCurrent() reads
 *   the DB directly on every call, closing F0's confirmed legacy restart-required caching quirk")
 * PUT  /settings/1 — singleton entity, id is always 1 (Phase 4 singleton pattern)
 */
export const settingsApi = {
  getCurrent: () => apiClient.get<Settings>('/settings/current'),
  update: (data: SettingsUpdateRequest) => apiClient.put<Settings>('/settings/1', data),
};
