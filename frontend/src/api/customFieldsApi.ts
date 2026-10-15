import { apiClient } from './client';
import type { CustomField, FieldTemplate } from './types';

// Request types for field definitions
export interface CustomFieldCreateRequest {
  name: string;
  field_type: 'textfield' | 'select' | 'textarea' | 'radio' | 'checkbox';
  required: boolean;
  options: string[] | null;
}

export type CustomFieldUpdateRequest = Partial<CustomFieldCreateRequest>;

// Request types for field templates
export interface FieldTemplateCreateRequest {
  name: string;
  context_id: string | null;
  field_ids: string[];
}

export type FieldTemplateUpdateRequest = Partial<FieldTemplateCreateRequest>;

export const customFieldsApi = {
  // Field Definitions
  getDefinitions: (params?: { context_id?: string }) =>
    apiClient.get<CustomField[]>('/custom-fields', { params }),

  getDefinitionById: (id: string) =>
    apiClient.get<CustomField>(`/custom-fields/${id}`),

  createDefinition: (data: CustomFieldCreateRequest) =>
    apiClient.post<CustomField>('/custom-fields', data),

  updateDefinition: (id: string, data: CustomFieldUpdateRequest) =>
    apiClient.put<CustomField>(`/custom-fields/${id}`, data),

  deleteDefinition: (id: string) =>
    apiClient.delete(`/custom-fields/${id}`),

  // Field Templates
  getTemplates: () =>
    apiClient.get<FieldTemplate[]>('/field-templates'),

  getTemplateById: (id: string) =>
    apiClient.get<FieldTemplate>(`/field-templates/${id}`),

  createTemplate: (data: FieldTemplateCreateRequest) =>
    apiClient.post<FieldTemplate>('/field-templates', data),

  updateTemplate: (id: string, data: FieldTemplateUpdateRequest) =>
    apiClient.put<FieldTemplate>(`/field-templates/${id}`, data),

  deleteTemplate: (id: string) =>
    apiClient.delete(`/field-templates/${id}`),
};
