import { apiClient } from './client';
import { CustomField, CustomFieldValue } from './types';

export const customFieldsApi = {
  getCustomFields: (params?: { context_id?: string }) =>
    apiClient.get<CustomField[]>('/custom-fields', { params }),

  getCustomFieldById: (id: string) =>
    apiClient.get<CustomField>(`/custom-fields/${id}`),

  createCustomField: (data: Omit<CustomField, 'id'>) =>
    apiClient.post<CustomField>('/custom-fields', data),

  updateCustomField: (id: string, data: Partial<Omit<CustomField, 'id'>>) =>
    apiClient.put<CustomField>(`/custom-fields/${id}`, data),

  deleteCustomField: (id: string) =>
    apiClient.delete(`/custom-fields/${id}`),

  getFieldValues: (bookingId: string) =>
    apiClient.get<CustomFieldValue[]>(`/bookings/${bookingId}/custom-field-values`),
};
