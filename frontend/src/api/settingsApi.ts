import { apiClient } from './client';

/**
 * Settings singleton entity shape (from Phase 4 settings-service).
 * The entity is always id=1; the frontend never sends the id in updates.
 */
export interface Settings {
  id: number; // always 1, singleton
  approve_booking: boolean;
  calendar_slot_size: number; // minutes: 15 | 30 | 60
  calendar_min_time: string; // "HH:mm" e.g. "08:00"
  calendar_max_time: string; // "HH:mm" e.g. "18:00"
}

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
