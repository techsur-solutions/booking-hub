import { apiClient } from './client';
import type { Resource } from './types';

export const resourcesApi = {
  getResources: (params?: { include_deleted?: boolean }) =>
    apiClient.get<Resource[]>('/resources', { params }),

  getResourceById: (id: string) =>
    apiClient.get<Resource>(`/resources/${id}`),

  createResource: (data: Omit<Resource, 'id' | 'deleted_at'>) =>
    apiClient.post<Resource>('/resources', data),

  updateResource: (id: string, data: Partial<Omit<Resource, 'id' | 'deleted_at'>>) =>
    apiClient.put<Resource>(`/resources/${id}`, data),

  deleteResource: (id: string) =>
    apiClient.delete(`/resources/${id}`),
};
