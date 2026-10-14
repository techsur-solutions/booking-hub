import { apiClient } from './client';
import type { User, Permission } from './types';

export const usersApi = {
  getUsers: (params?: { include_deleted?: boolean }) =>
    apiClient.get<User[]>('/users', { params }),

  getUserById: (id: string) =>
    apiClient.get<User>(`/users/${id}`),

  createUser: (data: {
    email: string;
    display_name: string;
    password: string;
  }) => apiClient.post<User>('/users', data),

  updateUser: (
    id: string,
    data: Partial<Pick<User, 'email' | 'display_name'>>
  ) => apiClient.put<User>(`/users/${id}`, data),

  deleteUser: (id: string) => apiClient.delete(`/users/${id}`),

  getPermissions: (userId: string) =>
    apiClient.get<Permission>(`/users/${userId}/permissions`),

  updatePermissions: (userId: string, data: Partial<Permission>) =>
    apiClient.put<Permission>(`/users/${userId}/permissions`, data),

  getCurrentUser: () => apiClient.get<User>('/users/me'),

  // Aliases used by auth flow pages (UI-AUTH-02)
  getMe: () => apiClient.get<User>('/users/me'),

  updateMe: (data: Partial<Pick<User, 'display_name'>>) =>
    apiClient.put<User>('/users/me', data),

  // Password reset endpoints (UI-AUTH-03, implemented in Phase 3)
  requestPasswordReset: (email: string) =>
    apiClient.post('/users/password-reset/request', { email }),

  completePasswordReset: (token: string, new_password: string) =>
    apiClient.post('/users/password-reset/complete', { token, new_password }),
};
