import { apiClient } from './client';
import type { Resource } from './types';

export interface ResourceCreateRequest {
  name: string;
  type?: string;
  description?: string;
  is_unique?: boolean;
  restrict_locations?: string[]; // location UUIDs
}

export interface ResourceUpdateRequest {
  name?: string;
  type?: string;
  description?: string;
  is_unique?: boolean;
  restrict_locations?: string[]; // location UUIDs
}

export const resourcesApi = {
  getAll: () => apiClient.get<Resource[]>('/resources'),
  getById: (id: string) => apiClient.get<Resource>(`/resources/${id}`),
  create: (data: ResourceCreateRequest) => apiClient.post<Resource>('/resources', data),
  update: (id: string, data: ResourceUpdateRequest) =>
    apiClient.put<Resource>(`/resources/${id}`, data),
  delete: (id: string) => apiClient.delete(`/resources/${id}`), // soft-delete
};
