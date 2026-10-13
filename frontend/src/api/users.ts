import { apiClient } from './client';
import type { User, Permission } from './types';

export const usersApi = {
  getUsers: (params?: { include_deleted?: boolean }) =>
    apiClient.get<User[]>('/users', { params }),

  getUserById: (id: string) =>
    apiClient.get<User>(`/users/${id}`),

  createUser: (data: {
    email: string;
    first_name: string;
    last_name: string;
    password: string;
  }) => apiClient.post<User>('/users', data),

  updateUser: (
    id: string,
    data: Partial<Pick<User, 'email' | 'first_name' | 'last_name'>>
  ) => apiClient.put<User>(`/users/${id}`, data),

  deleteUser: (id: string) => apiClient.delete(`/users/${id}`),

  getPermissions: (userId: string) =>
    apiClient.get<Permission>(`/users/${userId}/permissions`),

  updatePermissions: (userId: string, data: Partial<Permission>) =>
    apiClient.put<Permission>(`/users/${userId}/permissions`, data),

  getCurrentUser: () => apiClient.get<User>('/users/me'),
};
