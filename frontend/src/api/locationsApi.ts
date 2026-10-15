import { apiClient } from './client';
import type { Location } from './types';

export interface LocationCreateRequest {
  name: string;
  class?: string;
  colour: string;
  description?: string;
  building?: string;
  layouts?: string;
}

export interface LocationUpdateRequest {
  name?: string;
  class?: string;
  colour?: string;
  description?: string;
  building?: string;
  layouts?: string;
}

export const locationsApi = {
  getAll: () => apiClient.get<Location[]>('/locations'),
  getById: (id: string) => apiClient.get<Location>(`/locations/${id}`),
  create: (data: LocationCreateRequest) => apiClient.post<Location>('/locations', data),
  update: (id: string, data: LocationUpdateRequest) =>
    apiClient.put<Location>(`/locations/${id}`, data),
  delete: (id: string) => apiClient.delete(`/locations/${id}`), // soft-delete
};
