import { apiClient } from './client';
import { Settings } from './types';

export const settingsApi = {
  getSettings: () => apiClient.get<Settings>('/settings'),

  updateSettings: (data: Partial<Omit<Settings, 'id'>>) =>
    apiClient.put<Settings>('/settings', data),
};
