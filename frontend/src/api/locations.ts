import { apiClient } from './client';
import type { Location } from './types';

export const locationsApi = {
  getLocations: (params?: { include_deleted?: boolean }) =>
    apiClient.get<Location[]>('/locations', { params }),

  getLocationById: (id: string) =>
    apiClient.get<Location>(`/locations/${id}`),

  createLocation: (data: Omit<Location, 'id' | 'deleted_at'>) =>
    apiClient.post<Location>('/locations', data),

  updateLocation: (id: string, data: Partial<Omit<Location, 'id' | 'deleted_at'>>) =>
    apiClient.put<Location>(`/locations/${id}`, data),

  deleteLocation: (id: string) =>
    apiClient.delete(`/locations/${id}`),
};
