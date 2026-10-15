import { apiClient } from './client';
import type { User, Permission } from './types';

// ── Request/Response types ─────────────────────────────────────────────────────

export interface UserCreateRequest {
  email: string;
  display_name?: string;
  password: string;
  roles: string[];
}

export interface UserUpdateRequest {
  email?: string;
  display_name?: string;
  roles?: string[];
}

/** Self-update has NO password or role fields (compile-time guard matching Phase 3 backend type) */
export interface UserSelfUpdateRequest {
  display_name?: string;
}

export interface UserWithRoles extends User {
  roles: string[];
  enabled: boolean; // Keycloak enabled status (true = active, false = inactive)
}

// ── API client ─────────────────────────────────────────────────────────────────

export const usersApi = {
  /** GET /users — returns all users with roles (admin-only) */
  getAll: (params?: { include_deleted?: boolean }) =>
    apiClient.get<UserWithRoles[]>('/users', { params }),

  /** GET /users/:id */
  getById: (id: string) =>
    apiClient.get<UserWithRoles>(`/users/${id}`),

  /** POST /users — admin creates a user with roles */
  create: (data: UserCreateRequest) =>
    apiClient.post<UserWithRoles>('/users', data),

  /** PUT /users/:id — admin updates another user (display_name, email, roles) */
  update: (id: string, data: UserUpdateRequest) =>
    apiClient.put<UserWithRoles>(`/users/${id}`, data),

  /** PUT /users/me — self-service update (display_name only; type guards against password/roles) */
  updateSelf: (data: UserSelfUpdateRequest) =>
    apiClient.put<UserWithRoles>('/users/me', data),

  /** DELETE /users/:id */
  deleteUser: (id: string) => apiClient.delete(`/users/${id}`),

  /** PATCH /users/:id/enabled — admin-only enable/disable (Keycloak enabled flag) */
  setEnabled: (id: string, enabled: boolean) =>
    apiClient.patch<UserWithRoles>(`/users/${id}/enabled`, { enabled }),

  /** PUT /users/:id/roles — admin-only role assignment */
  updateRoles: (id: string, roles: string[]) =>
    apiClient.put(`/users/${id}/roles`, { roles }),

  /** GET /users/me — current user's own profile */
  getCurrentUser: () => apiClient.get<UserWithRoles>('/users/me'),

  // Aliases for auth flow pages (UI-AUTH-02)
  getMe: () => apiClient.get<UserWithRoles>('/users/me'),

  getUsers: (params?: { include_deleted?: boolean }) =>
    apiClient.get<UserWithRoles[]>('/users', { params }),

  getUserById: (id: string) =>
    apiClient.get<UserWithRoles>(`/users/${id}`),

  createUser: (data: {
    email: string;
    display_name?: string;
    password: string;
  }) => apiClient.post<UserWithRoles>('/users', data),

  updateUser: (
    id: string,
    data: Partial<Pick<User, 'email' | 'display_name'>>
  ) => apiClient.put<UserWithRoles>(`/users/${id}`, data),

  updateMe: (data: Partial<Pick<User, 'display_name'>>) =>
    apiClient.put<UserWithRoles>('/users/me', data),

  getPermissions: (userId: string) =>
    apiClient.get<Permission>(`/users/${userId}/permissions`),

  updatePermissions: (userId: string, data: Partial<Permission>) =>
    apiClient.put<Permission>(`/users/${userId}/permissions`, data),

  // Password reset endpoints (UI-AUTH-03, implemented in Phase 3)
  requestPasswordReset: (email: string) =>
    apiClient.post('/users/password-reset/request', { email }),

  completePasswordReset: (token: string, new_password: string) =>
    apiClient.post('/users/password-reset/complete', { token, new_password }),
};
