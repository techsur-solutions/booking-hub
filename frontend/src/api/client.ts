import axios from 'axios';
import { keycloak } from '../auth/keycloak';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

// Request interceptor: refresh token if needed, then inject JWT bearer token
apiClient.interceptors.request.use(
  async (config) => {
    if (keycloak.authenticated) {
      try {
        // Refresh token if it expires within 30 seconds
        await keycloak.updateToken(30);
        
        const token = keycloak.token;
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
      } catch (error) {
        console.error('[API Client] Token refresh failed:', error);
        // Token refresh failed - force re-login
        keycloak.login();
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: handle 401 (token expired/invalid)
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid — redirect to Keycloak login
      keycloak.login();
    }
    return Promise.reject(error);
  }
);
