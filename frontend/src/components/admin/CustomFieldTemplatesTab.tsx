import { useState, useCallback } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { customFieldsApi } from '../../api/customFieldsApi';
import type { FieldTemplateCreateRequest } from '../../api/customFieldsApi';
import type { FieldTemplate } from '../../api/types';
import { locationsApi } from '../../api/locations';
import { StateConfirmationBanner } from '../StateConfirmationBanner';

type FormMode = 'none' | 'create' | 'edit';

export function CustomFieldTemplatesTab() {
  const queryClient = useQueryClient();
  const [formMode, setFormMode] = useState<FormMode>('none');
  const [editingTemplate, setEditingTemplate] = useState<FieldTemplate | null>(null);
  const [banner, setBanner] = useState<{ action: string; entity: string; timestamp: Date } | null>(null);

  // Form state
  const [formName, setFormName] = useState('');
  const [formContextId, setFormContextId] = useState<string>('');
  const [formFieldIds, setFormFieldIds] = useState<string[]>([]);
  const [formError, setFormError] = useState('');

  const { data: templates, isLoading: loadingTemplates, isError: errorTemplates } = useQuery({
    queryKey: ['field-templates'],
    queryFn: () => customFieldsApi.getTemplates().then(r => r.data),
  });

  const { data: fields } = useQuery({
    queryKey: ['custom-fields'],
    queryFn: () => customFieldsApi.getDefinitions().then(r => r.data),
  });

  const { data: locations } = useQuery({
    queryKey: ['locations'],
    queryFn: () => locationsApi.getLocations().then(r => r.data),
  });

  const createMutation = useMutation({
    mutationFn: (data: FieldTemplateCreateRequest) => customFieldsApi.createTemplate(data),
    onSuccess: (response) => {
      queryClient.invalidateQueries({ queryKey: ['field-templates'] });
      setBanner({ action: 'Field template created', entity: response.data.name, timestamp: new Date() });
      resetForm();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: FieldTemplateCreateRequest }) =>
      customFieldsApi.updateTemplate(id, data),
    onSuccess: (response) => {
      queryClient.invalidateQueries({ queryKey: ['field-templates'] });
      setBanner({ action: 'Field template updated', entity: response.data.name, timestamp: new Date() });
      resetForm();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => customFieldsApi.deleteTemplate(id),
    onSuccess: (_data, id) => {
      queryClient.invalidateQueries({ queryKey: ['field-templates'] });
      const deleted = templates?.find(t => t.id === id);
      setBanner({ action: 'Field template deleted', entity: deleted?.name ?? '', timestamp: new Date() });
    },
  });

  const resetForm = useCallback(() => {
    setFormMode('none');
    setEditingTemplate(null);
    setFormName('');
    setFormContextId('');
    setFormFieldIds([]);
    setFormError('');
  }, []);

  const handleEdit = (template: FieldTemplate) => {
    setEditingTemplate(template);
    setFormName(template.name);
    setFormContextId(template.context_id ?? '');
    setFormFieldIds(template.field_ids);
    setFormMode('edit');
  };

  const handleDelete = (id: string) => {
    if (window.confirm('Delete this field template?')) {
      deleteMutation.mutate(id);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setFormError('');

    if (!formName.trim()) {
      setFormError('Template name is required.');
      return;
    }

    const data: FieldTemplateCreateRequest = {
      name: formName.trim(),
      context_id: formContextId || null,
      field_ids: formFieldIds,
    };

    if (formMode === 'edit' && editingTemplate) {
      updateMutation.mutate({ id: editingTemplate.id, data });
    } else {
      createMutation.mutate(data);
    }
  };

  const toggleFieldId = (fieldId: string) => {
    setFormFieldIds(prev =>
      prev.includes(fieldId) ? prev.filter(id => id !== fieldId) : [...prev, fieldId]
    );
  };

  const contextLabel = (contextId: string | null): string => {
    if (!contextId) return 'Global';
    const loc = locations?.find(l => l.id === contextId);
    return loc?.name ?? contextId;
  };

  const associatedFieldNames = (fieldIds: string[]): string => {
    if (fieldIds.length === 0) return '—';
    return fieldIds
      .map(fid => fields?.find(f => f.id === fid)?.name ?? fid)
      .join(', ');
  };

  return (
    <div className="custom-field-templates-tab" data-testid="custom-field-templates-tab">
      {banner && (
        <StateConfirmationBanner
          action={banner.action}
          entity={banner.entity}
          timestamp={banner.timestamp}
          onDismiss={() => setBanner(null)}
        />
      )}

      <div className="tab-header">
        <h2>Field Templates</h2>
        {formMode === 'none' && (
          <button
            type="button"
            className="btn btn--primary"
            data-testid="create-field-template-btn"
            onClick={() => setFormMode('create')}
          >
            Create Field Template
          </button>
        )}
      </div>

      {formMode !== 'none' && (
        <div className="form-panel" data-testid="field-template-form-panel">
          <h3>{formMode === 'edit' ? 'Edit Field Template' : 'Create Field Template'}</h3>
          <form onSubmit={handleSubmit} data-testid="field-template-form">
            {formError && (
              <div className="form-error" data-testid="template-form-error">{formError}</div>
            )}

            <div className="form-group">
              <label htmlFor="template-name">Template Name</label>
              <input
                id="template-name"
                type="text"
                value={formName}
                onChange={e => setFormName(e.target.value)}
                required
                maxLength={255}
                data-testid="template-name-input"
              />
            </div>

            <div className="form-group">
              <label htmlFor="template-context">Context (Location)</label>
              <select
                id="template-context"
                value={formContextId}
                onChange={e => setFormContextId(e.target.value)}
                data-testid="template-context-select"
              >
                <option value="">Global (applies to all locations)</option>
                {locations?.filter(l => !l.deleted_at).map(loc => (
                  <option key={loc.id} value={loc.id}>{loc.name}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>Associated Fields</label>
              <div className="field-checkboxes" data-testid="template-field-checkboxes">
                {fields?.map(field => (
                  <label key={field.id} className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={formFieldIds.includes(field.id)}
                      onChange={() => toggleFieldId(field.id)}
                      data-testid={`template-field-checkbox-${field.id}`}
                    />
                    {field.name} ({field.field_type})
                  </label>
                ))}
                {(!fields || fields.length === 0) && (
                  <p className="empty-msg">No field definitions available. Create some first.</p>
                )}
              </div>
            </div>

            <div className="form-actions">
              <button
                type="submit"
                className="btn btn--primary"
                data-testid="template-submit-btn"
                disabled={createMutation.isPending || updateMutation.isPending}
              >
                {formMode === 'edit' ? 'Update Template' : 'Create Template'}
              </button>
              <button
                type="button"
                className="btn btn--secondary"
                onClick={resetForm}
                data-testid="template-cancel-btn"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {loadingTemplates && <p>Loading field templates…</p>}
      {errorTemplates && <p className="error">Failed to load field templates.</p>}

      {!loadingTemplates && !errorTemplates && (
        <table className="admin-table" data-testid="field-templates-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Context</th>
              <th>Associated Fields</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {templates && templates.length === 0 && (
              <tr>
                <td colSpan={4} className="empty-row">No field templates yet.</td>
              </tr>
            )}
            {templates?.map(template => (
              <tr key={template.id} data-testid={`template-row-${template.id}`}>
                <td data-testid={`template-name-${template.id}`}>{template.name}</td>
                <td data-testid={`template-context-${template.id}`}>{contextLabel(template.context_id)}</td>
                <td data-testid={`template-fields-${template.id}`}>{associatedFieldNames(template.field_ids)}</td>
                <td className="actions-cell">
                  <button
                    type="button"
                    className="btn btn--sm btn--secondary"
                    data-testid={`edit-template-${template.id}`}
                    onClick={() => handleEdit(template)}
                  >
                    Edit
                  </button>
                  <button
                    type="button"
                    className="btn btn--sm btn--danger"
                    data-testid={`delete-template-${template.id}`}
                    onClick={() => handleDelete(template.id)}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
